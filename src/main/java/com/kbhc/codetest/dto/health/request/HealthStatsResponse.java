package com.kbhc.codetest.dto.health.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class HealthStatsResponse {
    private String date; // LocalDate 또는 YearMonth를 문자열로 전달
    private long totalSteps;
    private double totalCalories;
    private double totalDistance;
    private String deviceName; // 기기 정보 포함
}
