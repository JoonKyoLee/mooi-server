package com.example.emotion_storage.timecapsule.dto.response;

import com.example.emotion_storage.timecapsule.domain.TimeCapsule;
import com.example.emotion_storage.timecapsule.dto.PaginationDto;
import com.example.emotion_storage.timecapsule.dto.TimeCapsuleDto;
import java.util.List;
import org.springframework.data.domain.Page;

public record TimeCapsuleListResponse(
        PaginationDto pagination,
        int totalCapsules,
        List<TimeCapsuleDto> timeCapsules
) {
    public static TimeCapsuleListResponse of(Page<TimeCapsule> timeCapsules, int page, int limit) {
        return new TimeCapsuleListResponse(
                new PaginationDto(
                        page, limit, timeCapsules.getTotalPages()
                ),
                timeCapsules.getNumberOfElements(),
                timeCapsules.getContent().stream()
                        .map(TimeCapsuleDto::from)
                        .toList()
        );
    }
}
