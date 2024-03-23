package com.gameverse.scoremaster.subscriber;

import com.gameverse.scoremaster.exception.RetryWorkFlowException;
import com.gameverse.scoremaster.service.UserScoreUpdateEventService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class UserScoreUpdateEventListener {
    @Autowired
    UserScoreUpdateEventService userScoreUpdateEventService;

    @Retryable(value = {RetryWorkFlowException.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    @KafkaListener(topics = "${kafka.consumer.topic-name}", groupId = "${kafka.consumer.cache-group-id}", containerFactory = "batchListenerContainerFactory")
    public void eventListener(List<ConsumerRecord<String, String>> recordList) {
        try {
            userScoreUpdateEventService.processEventToCache(recordList);
            userScoreUpdateEventService.processEventToDb(recordList);
        } catch (Exception e) {
            log.error("Error while consuming userScores: {}", recordList, e.getMessage(), e);
            throw new RetryWorkFlowException("Error while consuming userScores: {}" + recordList);
        }
    }
}
