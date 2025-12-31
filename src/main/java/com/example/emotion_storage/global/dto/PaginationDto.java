package com.example.emotion_storage.global.dto;

public record PaginationDto(
        int page,
        int limit,
        int totalPage
) {}
