package com.gameverse.scoremaster.service;

import com.gameverse.scoremaster.dto.UserScore;
import com.gameverse.scoremaster.response.LeaderboardResponse;

import java.util.List;

public interface LeaderboardService {
    List<LeaderboardResponse> getLeaderboard(int count);
    List<UserScore> getUserScores(long userId);
}
