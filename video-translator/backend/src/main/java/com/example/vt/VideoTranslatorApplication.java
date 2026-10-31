package com.example.vt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class VideoTranslatorApplication {
    public static void main(String[] args) {
        SpringApplication.run(VideoTranslatorApplication.class, args);
    }
}
