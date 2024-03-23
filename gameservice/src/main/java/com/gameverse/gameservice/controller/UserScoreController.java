package com.gameverse.gameservice.controller;

import com.gameverse.gameservice.dto.request.UserScore;
import com.gameverse.gameservice.dto.response.ApiResponse;
import com.gameverse.gameservice.service.UserScoreService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.gameverse.gameservice.constant.CommonConstants.SUCCESS_MESSAGE;

@RestController
@Slf4j
public class UserScoreController {

    @Autowired
    UserScoreService userScoreService;

    @PostMapping("/scores")
    public ApiResponse<Object> pushScore(@RequestBody List<UserScore> userScores) {
        log.info("Received request to push user scores: {}", userScores);
        userScoreService.pushUserScore(userScores);
        return ApiResponse.builder().response(SUCCESS_MESSAGE).status(HttpStatus.OK.value()).build();
    }
}
