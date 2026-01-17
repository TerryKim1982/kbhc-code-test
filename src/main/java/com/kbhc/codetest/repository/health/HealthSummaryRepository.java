package com.kbhc.codetest.repository.health;

import com.kbhc.codetest.entity.health.HealthSummary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HealthSummaryRepository extends JpaRepository<HealthSummary, Long> {
    // 통계 데이터 중복 체크에 사용
    Optional<HealthSummary> findByMemberIdAndDeviceIdAndSummaryDate(Long memberId, Long deviceId, String summaryDate);

    List<HealthSummary> findAllByMemberIdAndDeviceIdAndSummaryDateBetween(Long memberId, Long deviceId, String startDate, String endDate);

}
