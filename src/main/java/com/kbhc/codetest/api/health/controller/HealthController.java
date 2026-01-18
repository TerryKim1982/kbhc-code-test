package com.kbhc.codetest.api.health.controller;

import com.kbhc.codetest.api.health.service.HealthService;
import com.kbhc.codetest.api.health.service.kafka.KafkaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/api/v1/health")
@RequiredArgsConstructor
public class HealthController {

    private final HealthService healthService;

    private final KafkaService kafkaService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadHealthData(@RequestPart("file") MultipartFile file,
                                            @AuthenticationPrincipal UserDetails userDetails) {
        return kafkaService.sendDateToKafka(userDetails.getUsername(), file);
    }

    @GetMapping(value = "/device")
    public ResponseEntity<?> getMyDeviceList(@AuthenticationPrincipal UserDetails userDetails) {
        return healthService.getMyDeviceList(userDetails.getUsername());
    }

    @GetMapping(value = "/stats/daily")
    public ResponseEntity<?> getDailyStats(@RequestHeader("Authorization") String token,
                                           @RequestParam Long deviceId, @RequestParam String start,
                                           @RequestParam String end) {
        return healthService.getDailyStats(token, deviceId, start, end);
    }

    @GetMapping(value = "/stats/monthly")
    public ResponseEntity<?> getMonthlyStats(@RequestHeader("Authorization") String token,
                                           @RequestParam Long deviceId, @RequestParam String start,
                                           @RequestParam String end) {
        return healthService.getMonthlyStats(token, deviceId, start, end);
    }
}
