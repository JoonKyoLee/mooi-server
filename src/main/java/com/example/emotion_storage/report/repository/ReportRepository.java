package com.example.emotion_storage.report.repository;

import com.example.emotion_storage.report.domain.Report;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {
    
    @Query("SELECT COUNT(DISTINCT r) FROM Report r " +
           "JOIN r.timeCapsules tc " +
           "WHERE tc.user.id = :userId AND r.isOpened = false")
    Long countUnopenedReportsByUserId(@Param("userId") Long userId);
    
    @Query("SELECT DISTINCT r FROM Report r " +
           "JOIN r.timeCapsules tc " +
           "WHERE tc.user.id = :userId AND r.historyDate = :historyDate")
    Optional<Report> findByUserIdAndHistoryDate(@Param("userId") Long userId, @Param("historyDate") LocalDate historyDate);

    @Query("""
        select distinct r.id
        from Report r
        join r.timeCapsules tc
        where tc.user.id = :userId
        order by r.historyDate desc
    """)
    List<Long> findLatestReportIdsByUserId(@Param("userId") Long userId, Pageable pageable);

    default Optional<Long> findLatestReportIdByUserId(Long userId) {
        return findLatestReportIdsByUserId(userId, Pageable.ofSize(1)).stream().findFirst();
    }

    @Query("""
        select distinct r.id
        from Report r
        join r.timeCapsules tc
        where tc.user.id = :userId
          and r.isOpened = false
        order by r.historyDate desc
    """)
    List<Long> findLatestUnopenedReportIdsByUserId(@Param("userId") Long userId, Pageable pageable);

    default Optional<Long> findLatestUnopenedReportIdByUserId(Long userId) {
        return findLatestUnopenedReportIdsByUserId(userId, Pageable.ofSize(1))
                .stream().findFirst();
    }
}
