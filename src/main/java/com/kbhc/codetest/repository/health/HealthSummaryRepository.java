package com.kbhc.codetest.repository.health;

import com.kbhc.codetest.entity.health.HealthSummary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface HealthSummaryRepository extends JpaRepository<HealthSummary, Long> {
    // 통계 데이터 중복 체크에 사용
    Optional<HealthSummary> findByMemberIdAndDeviceIdAndSummaryDate(Long memberId, Long deviceId, LocalDate summaryDate);

    List<HealthSummary> findAllByMemberIdAndDeviceIdAndSummaryDateBetween(Long memberId, Long deviceId, LocalDate startDate, LocalDate endDate);

}
