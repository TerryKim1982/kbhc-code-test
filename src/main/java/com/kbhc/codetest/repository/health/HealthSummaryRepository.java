package com.kbhc.codetest.repository.health;

import com.kbhc.codetest.entity.health.HealthSummary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface HealthSummaryRepository extends JpaRepository<HealthSummary, Long> {
    Optional<HealthSummary> findByMemberIdAndDeviceIdAndSummaryDate(Long memberId, Long deviceId, LocalDate summaryDate);
}
