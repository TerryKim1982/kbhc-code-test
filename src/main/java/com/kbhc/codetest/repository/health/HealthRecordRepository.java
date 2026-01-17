package com.kbhc.codetest.repository.health;

import com.kbhc.codetest.entity.health.Device;
import com.kbhc.codetest.entity.health.HealthRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;

public interface HealthRecordRepository extends JpaRepository<HealthRecord, Integer> {
    boolean existsByDeviceAndUploadTime(Device device, OffsetDateTime uploadTime);
}
