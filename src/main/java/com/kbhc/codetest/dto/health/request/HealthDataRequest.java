package com.kbhc.codetest.dto.health.request;

import com.kbhc.codetest.dto.health.kafka.KafkaHealthData;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class HealthDataRequest {
    private String email;
    private KafkaHealthData healthData;
}
