package com.kbhc.codetest.api.health.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kbhc.codetest.api.health.service.kafka.HealthDataProducer;
import com.kbhc.codetest.dto.health.kafka.KafkaHealthData;
import com.kbhc.codetest.dto.health.request.HealthDataRequest;
import com.kbhc.codetest.util.DateUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class HealthService {

    private final HealthDataProducer healthDataProducer;

    private final ObjectMapper objectMapper;

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
                HealthDataRequest healthDataRequest = new HealthDataRequest();
                healthDataRequest.setEmail(email);
                healthDataRequest.setHealthData(fullData);

                // Kafka Producer에게 데이터 전달
                healthDataProducer.send(healthDataRequest);
            }

        }
        catch (IOException e) {
            return ResponseEntity.internalServerError().body("파일 처리중 오류가 발생하였습니다.");
        }
        return ResponseEntity.ok("파일 업로드 및 데이터 분석 요청 완료 (파일명: " + file.getOriginalFilename() + ")");
    }

}
