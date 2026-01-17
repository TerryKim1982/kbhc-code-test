package com.kbhc.codetest.dto.health.kafka;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
public class KafkaHealthData {
    private String recordkey;
    private DataContent data;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss Z")
    private OffsetDateTime lastUpdate;
    private String type;

    @Getter
    @NoArgsConstructor
    public static class DataContent {
        @Setter
        private List<Entry> entries;
        private DeviceSource source;

    }

    @Getter
    @NoArgsConstructor
    public static class Entry {
        private Period period;
        private ValueUnit distance;
        private ValueUnit calories;
        private String steps;
    }

    @Getter
    @NoArgsConstructor
    public static class Period {
        private String from;
        private String to;
    }

    @Getter
    @NoArgsConstructor
    public static class ValueUnit {
        private String unit;
        private double value;
    }

    @Getter
    @NoArgsConstructor
    public static class DeviceSource {
        private int mode;
        private String name;
        private String type;
        private Product product;
    }

    @Getter
    @NoArgsConstructor
    public static class Product {
        private String name;
        private String vender;
    }
}
