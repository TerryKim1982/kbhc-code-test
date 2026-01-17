package com.kbhc.codetest.dto.health.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class MyDeviceResponse {
    private Long deviceId;
    private String recordKey;
    private String name;
}
