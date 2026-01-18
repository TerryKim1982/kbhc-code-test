package com.kbhc.codetest.api.health.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kbhc.codetest.api.auth.jwt.JwtTokenProvider;
import com.kbhc.codetest.api.health.service.kafka.HealthDataProducer;
import com.kbhc.codetest.dto.ApiResponse;
import com.kbhc.codetest.dto.health.kafka.KafkaHealthData;
import com.kbhc.codetest.dto.health.request.HealthDataSendRequest;
import com.kbhc.codetest.dto.health.response.HealthStatsResponse;
import com.kbhc.codetest.dto.health.response.MyDeviceResponse;
import com.kbhc.codetest.entity.health.Device;
import com.kbhc.codetest.entity.health.HealthSummary;
import com.kbhc.codetest.repository.health.DeviceRepository;
import com.kbhc.codetest.repository.health.HealthSummaryRepository;
import com.kbhc.codetest.util.DateUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class HealthService {

    private final HealthDataProducer healthDataProducer;

    private final ObjectMapper objectMapper;

    private final DeviceRepository deviceRepository;
    private final HealthSummaryRepository healthSummaryRepository;

    private final JwtTokenProvider jwtTokenProvider;

    public ResponseEntity<?> sendDateToKafka(String email, MultipartFile file) {
        try {
            // 파일 역직렬화
            KafkaHealthData fullData = objectMapper.readValue(file.getInputStream(), KafkaHealthData.class);

            OffsetDateTime lastUpdate = fullData.getLastUpdate();

            List<KafkaHealthData.Entry> filteredEntries = fullData.getData().getEntries().stream().toList().stream()
                    .filter(entry -> {
                        OffsetDateTime entryTime = DateUtils.parseToOffsetDateTime(entry.getPeriod().getFrom());
                        return entryTime.isAfter(lastUpdate);
                    })
                    .toList();
            log.info("필터링 전: {}건 -> 필터링 후: {}건", fullData.getData().getEntries().size(), filteredEntries.size());

            if(!filteredEntries.isEmpty()){
                // 필터된 데이터만 kafka로 전송
                fullData.getData().setEntries(filteredEntries);
                HealthDataSendRequest healthDataSendRequest = new HealthDataSendRequest();
                healthDataSendRequest.setEmail(email);
                healthDataSendRequest.setHealthData(fullData);

                // Kafka Producer에게 데이터 전달
                healthDataProducer.send(healthDataSendRequest);
            }

        }
        catch (IOException e) {
            return ResponseEntity.internalServerError().body("파일 처리중 오류가 발생하였습니다.");
        }
        return ResponseEntity.ok("파일 업로드 및 데이터 분석 요청 완료 (파일명: " + file.getOriginalFilename() + ")");
    }

    public ResponseEntity<?> getMyDeviceList(String email) {
        List<Device> myDeviceList = deviceRepository.findAllByMember_Email(email);
        if(myDeviceList.isEmpty()){
            return ResponseEntity.ok(ApiResponse.success(Collections.emptyList(), "등록된 기기가 없습니다."));
        }
        List<MyDeviceResponse> response = myDeviceList.stream().map(device ->
                MyDeviceResponse.builder()
                        .deviceId(device.getId())
                        .recordKey(device.getRecordKey())
                        .name(device.getName() + "(" + device.getProductVender() + ")")
                        .build())
                .toList();
        return ResponseEntity.ok(ApiResponse.success(response, "조회 성공"));
    }

    public ResponseEntity<?> getDailyStats(String jwtToken, Long deviceId, String start,  String end) {
        // Bearer 제거
        Long memberId = this.getMemberIdByToken(jwtToken);

        // 이 사용자의 소유 디바이스가 맞는지 확인
        if(this.checkIsNotOwner(deviceId, memberId)){
            return ResponseEntity.badRequest().body("잘못된 접근입니다.");
        }

        List<HealthStatsResponse> summaries = healthSummaryRepository.findAllByMemberIdAndDeviceIdAndSummaryDateBetween(memberId, deviceId, start, end).stream()
                .map(s -> HealthStatsResponse.builder()
                        .date(s.getSummaryDate())
                        .totalSteps(s.getTotalSteps())
                        .totalCalories(s.getTotalCalories())
                        .totalDistance(s.getTotalDistance())
                        .recordKey(s.getRecordKey())
                        .build()
                ).toList();
        if(summaries.isEmpty()){
            return ResponseEntity.ok(ApiResponse.success(Collections.emptyList(), "해당기간의 데이터가 없습니다."));
        }
        return ResponseEntity.ok(ApiResponse.success(summaries, "조회 성공"));
    }

    public ResponseEntity<?> getMonthlyStats(String jwtToken, Long deviceId, String start,  String end) {
        // Bearer 제거
        Long memberId = this.getMemberIdByToken(jwtToken);

        // 이 사용자의 소유 디바이스가 맞는지 확인
        if(this.checkIsNotOwner(deviceId, memberId)){
            return ResponseEntity.badRequest().body("잘못된 접근입니다.");
        }

        List<HealthStatsResponse> result = new ArrayList<>();
        YearMonth startMonth = YearMonth.parse(start);
        YearMonth endMonth = YearMonth.parse(end);
        YearMonth currentMonth = startMonth;
        while(!currentMonth.isAfter(endMonth)){
            String startDate = currentMonth.atDay(1).toString();
            String endDate = currentMonth.atEndOfMonth().toString();
            List<HealthSummary> dailyData =
                    healthSummaryRepository.findAllByMemberIdAndDeviceIdAndSummaryDateBetween(memberId, deviceId, startDate, endDate);
            // 조회 결과가 있으면 합산
            if(!dailyData.isEmpty()){
                HealthStatsResponse monthlySum = HealthStatsResponse.builder()
                        .date(currentMonth.toString()) // "2024-10"
                        .totalSteps(dailyData.stream().mapToLong(HealthSummary::getTotalSteps).sum())
                        .totalCalories(dailyData.stream().mapToDouble(HealthSummary::getTotalCalories).sum())
                        .totalDistance(dailyData.stream().mapToDouble(HealthSummary::getTotalDistance).sum())
                        .recordKey(dailyData.get(0).getRecordKey())
                        .build();
                result.add(monthlySum);
            }
            currentMonth = currentMonth.plusMonths(1);
        }
        if(result.isEmpty()){
            return ResponseEntity.ok(ApiResponse.success(Collections.emptyList(), "해당기간의 데이터가 없습니다."));
        }
        return ResponseEntity.ok(ApiResponse.success(result, "조회 성공"));
    }

    // 토큰에서 멤버아이디 뽑기
    private Long getMemberIdByToken(String jwtToken) {
        jwtToken = jwtToken.substring(7);
        return jwtTokenProvider.getMemberIdFromToken(jwtToken);
    }

    private boolean checkIsNotOwner(Long deviceId, Long memberId) {
        return !deviceRepository.existsByIdAndMember_Id(deviceId, memberId);
    }
}
