package com.kbhc.codetest.api.health.service.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kbhc.codetest.dto.ApiResponse;
import com.kbhc.codetest.dto.health.kafka.KafkaHealthData;
import com.kbhc.codetest.dto.health.request.HealthDataSendRequest;
import com.kbhc.codetest.util.DateUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaService {

    private final HealthDataProducer healthDataProducer;

    private final ObjectMapper objectMapper;

    public ResponseEntity<?> sendDateToKafka(String email, MultipartFile file) {
        try {
            // 파일 역직렬화
            KafkaHealthData fullData = objectMapper.readValue(file.getInputStream(), KafkaHealthData.class);

            // UTC 시간 기준을 한국표준시 기준 ZonedDateTime으로 변경
            ZonedDateTime lastUpdate = fullData.getLastUpdate().atZoneSameInstant(ZoneId.of("Asia/Seoul"));

            List<KafkaHealthData.Entry> filteredEntries = fullData.getData().getEntries().stream().toList().stream()
                    .filter(entry -> {
                        // 헬스킷과 삼성헬스와의 타임존이 다르므로 서울시간으로 맞춰서 변환(삼성헬스에는 한국시간으로 저장되어있다고 가정)
                        ZonedDateTime entryTime = DateUtils.parseToSeoulZone(entry.getPeriod().getFrom());
                        // 업데이트 시간 이후 수집된 데이터만 필터링
                        // 같은 ZonedDateTime으로 비교
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

                healthDataProducer.send(healthDataSendRequest);
            }

        }
        catch (IOException e) {
            return ResponseEntity.internalServerError().body("파일 처리중 오류가 발생하였습니다.");
        }
        return ResponseEntity.ok()
                .body(ApiResponse.success("파일 업로드 및 데이터 분석 요청 완료 (파일명: " + file.getOriginalFilename() + ")"));
    }
}
