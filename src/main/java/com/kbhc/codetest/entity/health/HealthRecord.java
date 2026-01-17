package com.kbhc.codetest.entity.health;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(uniqueConstraints = {
    @UniqueConstraint(name = "uk_device_upload_time", columnNames = {"device_id", "upload_time"})
})
public class HealthRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id")
    private Device device; // 어떤 디바이스에서 온 데이터인가?

    @Builder.Default
    @OneToMany(mappedBy = "healthRecord", cascade = CascadeType.ALL)
    private List<HealthDetail> details = new ArrayList<>();

    private OffsetDateTime uploadTime;

    public void addDetail(HealthDetail detail) {
        this.details.add(detail);
        detail.setHealthRecord(this);
    }
}