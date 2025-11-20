package com.example.emotion_storage.timecapsule.dto.response;

import com.example.emotion_storage.timecapsule.dto.EmotionDetailDto;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record TimeCapsuleCreateResponse(
        Long timeCapsuleId
) {}
