package com.kbhc.codetest.dto.health.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class HealthStatsResponse {
    private String date; // 일별은 날짜, 월별은 년월
    private long totalSteps;
    private double totalCalories;
    private double totalDistance;
    private String recordKey;
}
