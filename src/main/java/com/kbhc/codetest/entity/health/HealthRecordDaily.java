package com.kbhc.codetest.entity.health;

import com.kbhc.codetest.entity.member.Member;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "health_record_daily",
        indexes = @Index(name = "idx_daily_member_date", columnList = "member_id, record_date"))
public class HealthRecordDaily {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "record_date", nullable = false)
    private LocalDate recordDate;

    private int steps;

    private float calories;

    private float distance;

    @Column(length = 100)
    private String recordkey;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}

