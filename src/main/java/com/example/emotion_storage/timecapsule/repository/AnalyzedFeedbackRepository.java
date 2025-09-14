package com.example.emotion_storage.timecapsule.repository;

import com.example.emotion_storage.timecapsule.domain.AnalyzedFeedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AnalyzedFeedbackRepository extends JpaRepository<AnalyzedFeedback, Long> {
}
