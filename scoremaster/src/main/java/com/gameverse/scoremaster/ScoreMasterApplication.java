package com.gameverse.scoremaster;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@EnableRetry
public class ScoreMasterApplication {
    public static void main(String[] args) {
        SpringApplication.run(ScoreMasterApplication.class, args);
    }
}
