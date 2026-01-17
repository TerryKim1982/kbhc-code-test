package com.kbhc.codetest;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

@SpringBootApplication
public class CodeTestApplication {

    @PostConstruct
    public void started() {
        // 애플리케이션의 기본 타임존을 UTC로 설정
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

    public static void main(String[] args) {
        SpringApplication.run(CodeTestApplication.class, args);
    }


}
