package com.example.emotion_storage.timecapsule.dto;

public record PaginationDto(
        int page,
        int limit,
        int totalPage
) {}
