package com.gameverse.gameservice.dto.request;

import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;

@Data
public class UserScore {
    @NotNull(message = "User ID cannot be null")
    private Long userId;

    @NotNull(message = "Score cannot be null")
    @PositiveOrZero(message = "Score must be positive or zero")
    private Long score;

    @NotNull(message = "GameId cannot be null")
    private Long gameId;
}
