package com.gameverse.scoremaster.service;

import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.util.List;

public interface UserScoreUpdateEventService {
    void processEventToDb(List<ConsumerRecord<String, String>> recordList);
    void processEventToCache(List<ConsumerRecord<String, String>> recordList);
}
