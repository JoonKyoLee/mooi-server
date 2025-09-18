package com.example.emotion_storage.timecapsule.dto.response;

import com.example.emotion_storage.timecapsule.dto.PaginationDto;
import com.example.emotion_storage.timecapsule.dto.TimeCapsuleDto;
import java.util.List;

public record TimeCapsuleListResponse(
        PaginationDto pagination,
        int totalCapsules,
        List<TimeCapsuleDto> timeCapsules
) {}
