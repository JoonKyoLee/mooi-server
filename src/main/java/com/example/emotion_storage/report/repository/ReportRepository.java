package com.example.emotion_storage.report.repository;

import com.example.emotion_storage.report.domain.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {
    
    @Query("SELECT DISTINCT r FROM Report r " +
           "JOIN r.timeCapsules tc " +
           "WHERE tc.user.id = :userId AND r.isOpened = false")
    List<Report> findUnopenedReportsByUserId(@Param("userId") Long userId);
    
    @Query("SELECT COUNT(DISTINCT r) FROM Report r " +
           "JOIN r.timeCapsules tc " +
           "WHERE tc.user.id = :userId AND r.isOpened = false")
    Long countUnopenedReportsByUserId(@Param("userId") Long userId);
}
