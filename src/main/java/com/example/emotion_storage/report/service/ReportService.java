package com.example.emotion_storage.report.service;

import com.example.emotion_storage.global.exception.BaseException;
import com.example.emotion_storage.global.exception.ErrorCode;
import com.example.emotion_storage.report.domain.Report;
import com.example.emotion_storage.report.dto.response.DailyReportDetailResponse;
import com.example.emotion_storage.report.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportService {

    private final ReportRepository reportRepository;

    public DailyReportDetailResponse getDailyReportDetail(Long userId, String dateString) {
        log.info("일일리포트 상세 조회 요청 - userId: {}, date: {}", userId, dateString);
        
        LocalDate historyDate = parseDate(dateString);
        Report report = findReportByUserIdAndDate(userId, historyDate);
        report.open();
        
        DailyReportDetailResponse response = DailyReportDetailResponse.from(report);
        
        log.info("일일리포트 상세 조회 완료 - userId: {}, date: {}", userId, dateString);
        return response;
    }

    public DailyReportDetailResponse getDailyReportDetailById(Long userId, Long reportId) {
        log.info("일일리포트 상세 조회 요청 - userId: {}, reportId: {}", userId, reportId);

        Report report = findReportByReportId(reportId);
        report.open();

        DailyReportDetailResponse response = DailyReportDetailResponse.from(report);

        log.info("일일리포트 상세 조회 완료 - userId: {}, reportId: {}", userId, reportId);
        return response;
    }

    private LocalDate parseDate(String dateString) {
        try {
            return LocalDate.parse(dateString, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        } catch (DateTimeParseException e) {
            log.warn("잘못된 날짜 형식 - date: {}", dateString);
            throw new BaseException(ErrorCode.INVALID_DATE_FORMAT);
        }
    }

    private Report findReportByUserIdAndDate(Long userId, LocalDate historyDate) {

        return reportRepository.findByUserIdAndHistoryDate(userId, historyDate)
                .orElseThrow(() -> {
                    log.warn("존재하지 않는 일일리포트 조회 시도 - userId: {}, historyDate: {}", userId, historyDate);
                    return new BaseException(ErrorCode.REPORT_NOT_FOUND);
                });
    }

    private Report findReportByReportId(Long reportId) {
        return reportRepository.findById(reportId)
                .orElseThrow(() -> new BaseException(ErrorCode.REPORT_NOT_FOUND));
    }
}
