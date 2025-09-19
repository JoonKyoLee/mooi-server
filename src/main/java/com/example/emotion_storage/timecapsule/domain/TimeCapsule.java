package com.example.emotion_storage.timecapsule.domain;

import com.example.emotion_storage.global.entity.BaseTimeEntity;
import com.example.emotion_storage.report.domain.Report;
import com.example.emotion_storage.user.domain.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Table(name = "time_capsules")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class TimeCapsule extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "time_capsule_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_id", nullable = false)
    private Report report;

    @Column(name = "chatroom_id", nullable = false)
    private Long chatroomId;

    @Column(nullable = false)
    private LocalDateTime historyDate;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String oneLineSummary;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String dialogueSummary;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String myMindNote;

    private LocalDateTime favoriteAt;

    private LocalDateTime openedAt;

    @Column(nullable = false)
    private Boolean isOpened;

    @Column(nullable = false)
    private Boolean isTempSave;

    @Column(nullable = false)
    private Boolean isFavorite;

    private LocalDateTime deletedAt;

    @OneToMany(mappedBy = "timeCapsule", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<AnalyzedEmotion> analyzedEmotions = new ArrayList<>();

    @OneToMany(mappedBy = "timeCapsule", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<AnalyzedFeedback> analyzedFeedbacks = new ArrayList<>();

    public void addAnalyzedEmotion(AnalyzedEmotion emotion) {
        this.analyzedEmotions.add(emotion);
        emotion.setTimeCapsule(this);
    }

    public void addAnalyzedFeedback(AnalyzedFeedback feedback) {
        this.analyzedFeedbacks.add(feedback);
        feedback.setTimeCapsule(this);
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setReport(Report report) {
        this.report = report;
    }

    public void setFavoriteAt(LocalDateTime favoriteAt) {
        this.favoriteAt = favoriteAt;
    }

    public void setIsFavorite(Boolean isFavorite) {
        this.isFavorite = isFavorite;
    }

    public void setIsOpened(Boolean isOpened) {
        this.isOpened = true;
    }

    public void updateMyMindNote(String myMindNote) {
        this.myMindNote = myMindNote;
    }

    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }
}
