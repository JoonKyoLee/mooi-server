package com.example.emotion_storage.chat.controller;

import com.example.emotion_storage.chat.dto.response.SentimentAnalysisResponse;
import com.example.emotion_storage.chat.service.SentimentAnalysisService;
import com.example.emotion_storage.global.api.ApiResponse;
import com.example.emotion_storage.global.api.SuccessMessage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/sentiment")
@RequiredArgsConstructor
@Tag(name = "감정 분석", description = "감정 분석 관련 API")
public class SentimentAnalysisController {

    private final SentimentAnalysisService sentimentAnalysisService;

    @PostMapping("/analyze")
    @Operation(summary = "감정 분석", description = "AI 서버를 통해 감정 분석을 수행합니다.")
    public ResponseEntity<ApiResponse<SentimentAnalysisResponse>> analyzeSentiment() {
        log.info("감정 분석 요청을 받았습니다.");
        
        try {
            SentimentAnalysisResponse response = sentimentAnalysisService.analyzeSentiment();
            
            log.info("감정 분석이 성공적으로 완료되었습니다.");
            log.info("분석 결과 - 요약 개수: {}, 키워드: {}, 감정 변화: {}, 스트레스: {}, 행복: {}", 
                    response.getSummaries().size(), 
                    response.getKeywords(), 
                    response.getSentimentChanges().size(),
                    response.getStressLevel(),
                    response.getHappinessLevel());
            log.info("감정 리뷰: {}", response.getSentimentReview());
            
            return ResponseEntity.ok(
                    ApiResponse.success(SuccessMessage.SENTIMENT_ANALYSIS_SUCCESS.getMessage(), response)
            );
            
        } catch (Exception e) {
            log.error("감정 분석 중 오류 발생", e);
            throw e;
        }
    }
}
