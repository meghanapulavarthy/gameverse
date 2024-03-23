package com.gameverse.scoremaster.service.impl;

import com.gameverse.scoremaster.dto.UserScore;
import com.gameverse.scoremaster.entity.ScoreEntity;
import com.gameverse.scoremaster.entity.UserEntity;
import com.gameverse.scoremaster.repository.ScoreRepository;
import com.gameverse.scoremaster.response.LeaderboardResponse;
import com.gameverse.scoremaster.repository.UserRepository;
import com.gameverse.scoremaster.service.LeaderboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.gameverse.scoremaster.constants.CommonConstants.REDIS_KEY;

@Component
public class LeaderboardServiceImpl implements LeaderboardService {

    @Autowired
    private RedisTemplate<String, Long> redisTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ScoreRepository scoreRepository;

    @Override
    public List<LeaderboardResponse> getLeaderboard(int count) {
        List<UserScore> leaderboard = redisTemplate.opsForZSet().reverseRange(REDIS_KEY, 0, count - 1).stream().map(userId -> new UserScore(userId, Math.round(redisTemplate.opsForZSet().score(REDIS_KEY, userId)), null)).collect(Collectors.toList());
        List<Long> userIds = leaderboard.stream().map(UserScore::getUserId).collect(Collectors.toList());
        Map<Long, String> userIdToNameMap = userRepository.findAllByIdIn(userIds).stream().collect(Collectors.toMap(UserEntity::getId, UserEntity::getUsername));
        List<LeaderboardResponse> leaderboardResponses = leaderboard.stream().map(userScore -> new LeaderboardResponse(userIdToNameMap.get(userScore.getUserId()), userScore.getScore())).collect(Collectors.toList());
        return leaderboardResponses;
    }

    @Override
    public List<UserScore> getUserScores(long userId) {
        Optional<List<ScoreEntity>> scoreEntities = scoreRepository.getScoreEntity(userId);
        List<ScoreEntity> scoreEntityList = (List<ScoreEntity>) scoreEntities.get();
        List<UserScore> userScoreResponse = new ArrayList<>();
        for(ScoreEntity scoreEntity: scoreEntityList) {
            UserScore userScore = new UserScore();
            userScore.setUserId(scoreEntity.getUserId());
            userScore.setScore(scoreEntity.getScore());
            userScore.setGameId(scoreEntity.getGameId());
            userScoreResponse.add(userScore);
        }
        return userScoreResponse;
    }
}
