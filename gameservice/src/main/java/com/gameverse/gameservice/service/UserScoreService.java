package com.gameverse.gameservice.service;

import com.gameverse.gameservice.dto.request.UserScore;

import java.util.List;

public interface UserScoreService {
    void pushUserScore(List<UserScore> userScores);
}
