package com.gameverse.scoremaster.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gameverse.scoremaster.dto.UserScore;
import com.gameverse.scoremaster.entity.ScoreEntity;
import com.gameverse.scoremaster.entity.UserEntity;
import com.gameverse.scoremaster.repository.ScoreRepository;
import com.gameverse.scoremaster.repository.UserRepository;
import com.gameverse.scoremaster.service.UserScoreUpdateEventService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.gameverse.scoremaster.constants.CommonConstants.REDIS_KEY;

@Component
@Slf4j
public class UserScoreUpdateEventServiceImpl implements UserScoreUpdateEventService {

    @Value("${redis.max-records}")
    private Integer maxRecords;

    @Autowired
    private RedisTemplate<String, Long> redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ScoreRepository scoreRepository;

    @Override
    public void processEventToDb(List<ConsumerRecord<String, String>> recordList) {
        saveUserScoresToDb(parseUserScore(recordList));
    }

    @Override
    public void processEventToCache(List<ConsumerRecord<String, String>> recordList) {
       saveUserScoresToCache(parseUserScore(recordList));
    }

    private void saveUserScoresToCache(List<UserScore> userScores) {
        redisTemplate.executePipelined((RedisCallback<Void>) connection -> {
            for (UserScore userScore : userScores) {
                String userId = String.valueOf(userScore.getUserId());
                Boolean exists = redisTemplate.opsForZSet().score(REDIS_KEY, userId) != null;
                Long totalScore = 0L;
                if (!exists) {
                    totalScore = getUserFromDb(userScore.getUserId());
                }
                connection.zIncrBy(REDIS_KEY.getBytes(), totalScore+userScore.getScore(), userId.getBytes());
            }
            return null;
        });
        trimUserScores();
    }

    private void trimUserScores() {
        Long size = redisTemplate.opsForZSet().size(REDIS_KEY);
        if (size != null && size > maxRecords) {
            Long count = size - maxRecords;
            Set<Long> usersToRemove = redisTemplate.opsForZSet().range(REDIS_KEY, 0, count - 1);
            if (usersToRemove != null) {
                redisTemplate.opsForZSet().remove(REDIS_KEY, usersToRemove.toArray());
            }
        }
    }

    private Long getUserFromDb (long id) {
        Optional<UserEntity> userEntityOptional = userRepository.findById(id);
        return userEntityOptional.isPresent()?userEntityOptional.get().getTotalScore():0L;
    }

    private List<UserScore> parseUserScore (List<ConsumerRecord<String, String>> recordList) {
        return recordList.parallelStream().map(record -> {
            try {
                return objectMapper.readValue(record.value(), UserScore.class);
            } catch (Exception e) {
                log.error("Error parsing record: {}", record.value(), e);
                return null;
            }
        }).collect(Collectors.toList());
    }

    @Transactional
    private void saveUserScoresToDb(List<UserScore> userScores) {
        try {
            List<Long> userIds = userScores.stream().map(UserScore::getUserId).collect(Collectors.toList());
            Map<Long, UserEntity> userMap = userRepository.findAllById(userIds).stream().collect(Collectors.toMap(UserEntity::getId, userEntity -> userEntity));

            for (UserScore userScore : userScores) {
                UserEntity userEntity = userMap.getOrDefault(userScore.getUserId(), new UserEntity());
                long currentScore = userEntity.getTotalScore();
                long newScore = currentScore + userScore.getScore();
                userEntity.setTotalScore(newScore);
                userMap.put(userEntity.getId(), userEntity);
            }

            userRepository.saveAll(userMap.values());

            List<ScoreEntity> scoreEntities = userScores.stream().map(userScore -> {
                ScoreEntity scoreEntity = new ScoreEntity();
                scoreEntity.setUserId(userScore.getUserId());
                scoreEntity.setScore(userScore.getScore());
                scoreEntity.setGameId(userScore.getGameId());
                return scoreEntity;
            }).collect(Collectors.toList());
            scoreRepository.saveAll(scoreEntities);
        } catch (Exception e) {
            log.error("Error saving user scores: {}", e.getMessage(), e);
            throw new RuntimeException("Error saving user scores", e);
        }
    }
}
