package com.example.emotion_storage.timecapsule.dto.response;

import java.time.LocalDateTime;

public record TimeCapsuleFavoriteResponse(
        boolean isFavorite,
        LocalDateTime favoriteAt,
        int favoritesCnt
) {}
