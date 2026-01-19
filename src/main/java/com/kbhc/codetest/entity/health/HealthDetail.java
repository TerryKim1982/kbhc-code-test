package com.kbhc.codetest.entity.health;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private int steps;
    private double distance;
    private double calories;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "health_record_id")
    private HealthRecord healthRecord;

}