package com.example.emotion_storage.report.repository;

import com.example.emotion_storage.report.domain.EmotionVariation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmotionVariationRepository extends JpaRepository<EmotionVariation, Long> {
}
