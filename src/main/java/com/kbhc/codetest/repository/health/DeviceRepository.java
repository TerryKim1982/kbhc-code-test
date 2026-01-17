package com.kbhc.codetest.repository.health;

import com.kbhc.codetest.entity.health.Device;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DeviceRepository extends JpaRepository<Device, Long> {
    Optional<Device> findByRecordKey(String recordkey);

    List<Device> findAllByMember_Email(String email);

    boolean existsByIdAndMember_Id(Long id, Long memberId);
}
