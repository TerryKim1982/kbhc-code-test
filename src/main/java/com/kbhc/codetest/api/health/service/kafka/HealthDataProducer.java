package com.kbhc.codetest.api.health.service.kafka;

import com.kbhc.codetest.dto.health.kafka.KafkaHealthData;
import com.kbhc.codetest.dto.health.request.HealthDataRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HealthDataProducer {

    private final KafkaTemplate<String, KafkaHealthData> kafkaTemplate;
    private static final String TOPIC = "health-record-topic";

    public void send(HealthDataRequest request) {
        // recordkey를 Kafka의 메시지 키로 사용하여 동일 사용자의 데이터는 동일 파티션으로 전송
        kafkaTemplate.send(TOPIC, request.getEmail(), request.getHealthData());
    }
}
