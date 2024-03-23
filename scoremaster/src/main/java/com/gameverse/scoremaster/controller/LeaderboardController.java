package com.gameverse.scoremaster.controller;

import com.gameverse.scoremaster.response.ApiResponse;
import com.gameverse.scoremaster.service.LeaderboardService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import static com.gameverse.scoremaster.constants.CommonConstants.SUCCESS_MESSAGE;

@RestController
@Slf4j
public class LeaderboardController {

    @Autowired
    LeaderboardService leaderboardService;

    @GetMapping("/leaderboard/{count}")
    public ApiResponse<Object> getLeaderboard(@PathVariable Integer count) {
        log.info("fetching top {} players", count);
        return ApiResponse.builder().response(leaderboardService.getLeaderboard(count)).message(String.format(SUCCESS_MESSAGE, count)).status(HttpStatus.OK.value()).build();
    }

    @GetMapping("/game/scores/{userId}")
    public ApiResponse<Object> getUserScore(@PathVariable("userId") long userId) {
        return ApiResponse.builder().response(leaderboardService.getUserScores(userId)).message("SUCCESS").status(HttpStatus.OK.value()).build();
    }
}
