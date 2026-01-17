package com.kbhc.codetest.entity.health;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(uniqueConstraints = {
    @UniqueConstraint(columnNames = {"member_id", "device_id", "summaryDate"})
})
public class HealthSummary {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long memberId;

    private Long deviceId;

    private LocalDate summaryDate; // 2024-11-15 형태

    private int totalSteps;
    private double totalCalories;
    private double totalDistance;

    // 통계치 업데이트 메서드
    public void addData(int steps, double calories, double distance) {
        this.totalSteps += steps;
        this.totalCalories += calories;
        this.totalDistance += distance;
    }
}
