package com.example.emotion_storage.home.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NewTimeCapsuleResponse {
    
    private final boolean hasNewCapsule;
    private final int count;
}
