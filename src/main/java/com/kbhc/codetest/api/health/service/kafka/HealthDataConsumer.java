package com.kbhc.codetest.api.health.service.kafka;

import com.kbhc.codetest.dto.health.kafka.KafkaHealthData;
import com.kbhc.codetest.entity.health.Device;
import com.kbhc.codetest.entity.health.HealthDetail;
import com.kbhc.codetest.entity.health.HealthRecord;
import com.kbhc.codetest.entity.health.HealthSummary;
import com.kbhc.codetest.entity.member.Member;
import com.kbhc.codetest.exception.NotFoundException;
import com.kbhc.codetest.repository.health.DeviceRepository;
import com.kbhc.codetest.repository.health.HealthRecordRepository;
import com.kbhc.codetest.repository.health.HealthSummaryRepository;
import com.kbhc.codetest.repository.member.MemberRepository;
import com.kbhc.codetest.util.DateUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class HealthDataConsumer {

    private final DeviceRepository deviceRepository;
    private final MemberRepository memberRepository;
    private final HealthRecordRepository healthRecordRepository;
    private final HealthSummaryRepository healthSummaryRepository;

    @KafkaListener(topics = "health-record-topic", groupId = "health-group")
    @Transactional
    public void consume(@Payload KafkaHealthData healthData, @Header(KafkaHeaders.RECEIVED_KEY) String email) {

        log.info("Kafka 메시지 수신 - 사용자: {}, recordkey: {}", email, healthData.getRecordkey());

        // 1. Device 자동 등록(없으면)
        Device device = deviceRepository.findByRecordKey(healthData.getRecordkey()).
                orElseGet(() -> {
                    log.info("새로운 디바이스 감지, 자동 등록 진행: {}", healthData.getRecordkey());
                    Member member = memberRepository.findByEmail(email)
                            .orElseThrow(() ->
                                new NotFoundException("존재하지 않는 사용자입니다: " + email));
                    Device newDevice = Device.builder()
                            .recordKey(healthData.getRecordkey())
                            .mode(healthData.getData().getSource().getMode())
                            .name(healthData.getData().getSource().getName())
                            .type(healthData.getData().getSource().getType())
                            .productName(healthData.getData().getSource().getProduct().getName())
                            .productVender(healthData.getData().getSource().getProduct().getVender())
                            .member(member)
                            .build();
            return deviceRepository.save(newDevice);
        });

        OffsetDateTime currentUploadTime = healthData.getLastUpdate();
        // 2. 이미 해당 시간의 기록이 있는지 확인(중복 등록 방지)
        if (healthRecordRepository.existsByDeviceAndUploadTime(device, currentUploadTime)) {
            log.warn("이미 처리된 중복 데이터입니다. Device: {}, Time: {}", device.getRecordKey(), currentUploadTime);
            return;
        }

        // 3. HealthRecord 생성
        HealthRecord healthRecord = HealthRecord.builder()
                .device(device)
                .uploadTime(healthData.getLastUpdate())
                .build();

        // 4. 자식 엔티티(HealthDetail) 변환 및 연관관계 편의 메서드로 추가
        healthData.getData().getEntries().forEach(entry -> {

            // 기기간 데이터형태가 달라 맞추는 작업
            OffsetDateTime startLocal = DateUtils.parseToOffsetDateTime(entry.getPeriod().getFrom());
            OffsetDateTime endLocal = DateUtils.parseToOffsetDateTime(entry.getPeriod().getTo());
            int steps = (int) Math.round(Double.parseDouble(entry.getSteps()));

            HealthDetail detail = HealthDetail.builder()
                    .startTime(startLocal)
                    .endTime(endLocal)
                    .steps(steps)
                    .distance(entry.getDistance().getValue())
                    .calories(entry.getCalories().getValue())
                    .build();

            healthRecord.addDetail(detail); // 부모-자식 양방향 매핑

            // 통계 데이터(일별) 처리
            LocalDate summaryDate = startLocal.toLocalDate(); // OffsetDateTime -> LocalDate
            updateHealthSummary(device.getMember().getId(), device.getId(), summaryDate, entry);
        });

        healthRecordRepository.save(healthRecord);
        log.info("Kafka 메시지 처리완료 - 사용자: {}, recordkey: {}", email, healthData.getRecordkey());

        // 저장이 끝났다면 사용자의 lastUpdate 갱신 로직 추가
    }

    /**
     * 일별 통계 테이블 정보를 갱신합니다.
     */
    private void updateHealthSummary(Long memberId, Long deviceId, LocalDate date, KafkaHealthData.Entry entry) {
        HealthSummary summary = healthSummaryRepository.findByMemberIdAndDeviceIdAndSummaryDate(memberId, deviceId, date)
                .orElseGet(() ->
                        healthSummaryRepository.save(
                        HealthSummary.builder()
                                .memberId(memberId)
                                .deviceId(deviceId)
                                .summaryDate(date)
                                .totalSteps(0)
                                .totalCalories(0.0)
                                .totalDistance(0.0)
                                .build()
                ));

        summary.addData(
                (int) Math.round(Double.parseDouble(entry.getSteps())),
                entry.getCalories().getValue(),
                entry.getDistance().getValue()
        );
    }
}
