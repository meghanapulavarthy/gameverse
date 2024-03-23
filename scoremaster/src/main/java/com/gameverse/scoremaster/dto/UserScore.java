package com.gameverse.scoremaster.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserScore {
    private Long userId;
    private Long score;
    private Long gameId;
}
