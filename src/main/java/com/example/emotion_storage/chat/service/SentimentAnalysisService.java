package com.example.emotion_storage.chat.service;

import com.example.emotion_storage.chat.dto.request.SentimentAnalysisRequest;
import com.example.emotion_storage.chat.dto.response.SentimentAnalysisErrorResponse;
import com.example.emotion_storage.chat.dto.response.SentimentAnalysisResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class SentimentAnalysisService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${ai.server.base-url:http://localhost:8000}")
    private String aiServerBaseUrl;

    public SentimentAnalysisResponse analyzeSentiment() {
        try {
            SentimentAnalysisRequest request = SentimentAnalysisRequest.builder()
                    .roleMessage(SentimentAnalysisPrompts.ROLE_MESSAGE)
                    .referenceMessage(SentimentAnalysisPrompts.REFERENCE_MESSAGE)
                    .analyzeMessage(SentimentAnalysisPrompts.ANALYZE_MESSAGE)
                    .build();

            String url = aiServerBaseUrl + "/sentiment/analyze";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<SentimentAnalysisRequest> requestEntity = new HttpEntity<>(request, headers);
            
            log.info("AI 서버에 감정 분석 요청을 전송합니다. URL: {}", url);
            log.info("요청 데이터: {}", objectMapper.writeValueAsString(request));
            
            ResponseEntity<SentimentAnalysisResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    SentimentAnalysisResponse.class
            );
            
            SentimentAnalysisResponse responseBody = response.getBody();
            log.info("AI 서버로부터 감정 분석 응답을 받았습니다: {}", objectMapper.writeValueAsString(responseBody));
            
            return responseBody;
            
        } catch (HttpClientErrorException e) {
            log.error("AI 서버 클라이언트 오류 (4xx): {}", e.getResponseBodyAsString(), e);
            throw new RuntimeException("AI 서버 요청 오류: " + e.getMessage(), e);
            
        } catch (HttpServerErrorException e) {
            log.error("AI 서버 서버 오류 (5xx): {}", e.getResponseBodyAsString(), e);
            
            // 500 에러 응답 파싱 시도
            try {
                SentimentAnalysisErrorResponse errorResponse = objectMapper.readValue(
                        e.getResponseBodyAsString(), 
                        SentimentAnalysisErrorResponse.class
                );
                log.error("AI 서버 에러 상세: {}", errorResponse.getDetail());
            } catch (Exception parseException) {
                log.warn("에러 응답 파싱 실패: {}", parseException.getMessage());
            }
            
            throw new RuntimeException("AI 서버 내부 오류: " + e.getMessage(), e);
            
        } catch (Exception e) {
            log.error("감정 분석 요청 중 예상치 못한 오류 발생", e);
            throw new RuntimeException("감정 분석 요청 실패: " + e.getMessage(), e);
        }
    }
}
