package com.gameverse.gameservice.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gameverse.gameservice.dto.request.UserScore;
import com.gameverse.gameservice.exception.RetryWorkFlowException;
import com.gameverse.gameservice.producer.KafkaProducerClient;
import com.gameverse.gameservice.service.UserScoreService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@Retryable(value = {RetryWorkFlowException.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000))
public class UserScoreServiceImpl implements UserScoreService {

    @Autowired
    KafkaProducerClient kafkaProducerClient;

    @Autowired
    ObjectMapper objectMapper;

    @Override
    public void pushUserScore(List<UserScore> userScores) {
        log.info("Pushing user scores to Kafka: {}", userScores);
        try {
            userScores.forEach(userScore -> {
                try {
                    kafkaProducerClient.sendMessage(String.valueOf(userScore.getUserId()), objectMapper.writeValueAsString(userScore));
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            log.error("Error occurred while pushing user scores to Kafka: {}", userScores, e.getMessage(), e);
            throw new RetryWorkFlowException("Error occurred while pushing to kafka: " + e.getMessage());
        }
    }
}
//enhancement - push to dlq after retries exhaustion

