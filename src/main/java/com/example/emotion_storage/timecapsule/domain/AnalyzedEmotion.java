package com.example.emotion_storage.timecapsule.domain;

import com.example.emotion_storage.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Table(name = "analyzed_emotion")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class AnalyzedEmotion extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "analyzed_emotion_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "time_capsule_id", nullable = false)
    private TimeCapsule timeCapsule;

    @Column(name = "analyzed_emotion", columnDefinition = "TEXT", nullable = false)
    private String analyzedEmotion;

    public void setTimeCapsule(TimeCapsule timeCapsule) {
        this.timeCapsule = timeCapsule;
    }
}
