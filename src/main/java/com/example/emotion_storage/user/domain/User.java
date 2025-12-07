package com.example.emotion_storage.user.domain;

import com.example.emotion_storage.chat.domain.ChatRoom;
import com.example.emotion_storage.global.entity.BaseTimeEntity;
import com.example.emotion_storage.global.exception.BaseException;
import com.example.emotion_storage.global.exception.ErrorCode;
import com.example.emotion_storage.notification.domain.Notification;
import com.example.emotion_storage.timecapsule.domain.TimeCapsule;
import jakarta.annotation.Nullable;
import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

@Entity
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_users_email_deleted_at",
                        columnNames = {"email", "deleted_at"}
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE users SET deleted_at = NOW() WHERE user_id = ?")
@Where(clause = "deleted_at IS NULL")
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SocialType socialType;

    @Column(nullable = false)
    private String socialId;

    @Column(nullable = false)
    private String email;

    private String profileImageUrl;

    @Column(nullable = false)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender;

    @Column(nullable = false)
    private LocalDate birthday;

    @Column(nullable = false)
    private Long keyCount;

    @Column(nullable = false)
    private Long ticketCount;

    @ElementCollection
    @CollectionTable(name = "user_expectations", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "expectation", nullable = false, length = 100)
    @Builder.Default
    private List<String> expectations = new ArrayList<>(); // 추후 확인 필요

    @Column(nullable = false)
    private boolean isTermsAgreed;

    @Column(nullable = false)
    private boolean isPrivacyAgreed;

    @Column(nullable = false)
    private boolean isMarketingAgreed;

    @Column(nullable = false)
    private boolean appPushNotify;

    @Column(nullable = false)
    private boolean emotionReminderNotify;

    @ElementCollection
    @CollectionTable(name = "user_emotion_reminder_days", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "emotion_reminder_day")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Set<DayOfWeek> emotionReminderDays = new LinkedHashSet<>();

    private LocalTime emotionReminderTime;

    @Column(nullable = false)
    private boolean timeCapsuleReportNotify;

    @Column(nullable = false)
    private boolean marketingInfoNotify;

    @Column(nullable = false)
    private int attendanceStreak;

    private LocalDate lastAttendanceRewardDate;

    private LocalDateTime deletedAt;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ChatRoom> chatRooms = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TimeCapsule> timeCapsules = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Notification> notifications = new ArrayList<>();

    public void addChatRoom(ChatRoom room) {
        this.chatRooms.add(room);
        room.setUser(this);
    }

    public void removeChatRoom(ChatRoom room) {
        this.chatRooms.remove(room);
        room.setUser(null);
    }

    public void addTimeCapsule(TimeCapsule timeCapsule) {
        this.timeCapsules.add(timeCapsule);
        timeCapsule.setUser(this);
    }

    public void removeTimeCapsule(TimeCapsule timeCapsule) {
        this.timeCapsules.remove(timeCapsule);
        timeCapsule.setUser(null);
    }

    public void addNotification(Notification notification) {
        this.notifications.add(notification);
        notification.setUser(this);
    }

    public void removeNotification(Notification notification) {
        this.notifications.remove(notification);
        notification.setUser(null);
    }

    public void consumeKeys(Long requiredKeys) {
        if (this.keyCount < requiredKeys) {
            throw new BaseException(ErrorCode.TIME_CAPSULE_KEY_NOT_ENOUGH);
        }
        this.keyCount -= requiredKeys;
    }

    public void initTicketCount() {
        this.ticketCount = 10L;
    }

    public void useTicket() {
        if (this.ticketCount <= 0) {
            throw new BaseException(ErrorCode.TICKET_NOT_ENOUGH);
        }
        this.ticketCount -= 1;
    }

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    public void updateAppPushNotify(boolean appPushNotify) {
        this.appPushNotify = appPushNotify;
    }

    public void updateEmotionReminder(
            boolean emotionReminderNotify, @Nullable Set<DayOfWeek> emotionReminderDays, @Nullable LocalTime emotionReminderTime
    ) {
        this.emotionReminderNotify = emotionReminderNotify;

        if (!emotionReminderNotify) {
            this.emotionReminderDays = null;
            this.emotionReminderTime = null;
            return;
        }

        if (emotionReminderDays == null || emotionReminderDays.isEmpty()) throw new BaseException(ErrorCode.EMOTION_REMINDER_DAYS_REQUIRED);
        if (emotionReminderTime == null) throw new BaseException(ErrorCode.EMOTION_REMINDER_TIME_REQUIRED);

        this.emotionReminderDays.clear();
        this.emotionReminderDays.addAll(emotionReminderDays);
        this.emotionReminderTime = emotionReminderTime;
    }

    public void updateTimeCapsuleReportNotify(boolean timeCapsuleReportNotify) {
        this.timeCapsuleReportNotify = timeCapsuleReportNotify;
    }

    public void updateMarketingInfoNotify(boolean marketingInfoNotify) {
        this.marketingInfoNotify = marketingInfoNotify;
    }

    public void withdrawUser() {
        this.deletedAt = LocalDateTime.now();
    }

    public boolean isKakaoType() {
        return socialType.equals(SocialType.KAKAO);
    }

    public boolean isGoogleType() {
        return socialType.equals(SocialType.GOOGLE);
    }

    public void updateAttendanceStatus(int attendanceStreak, LocalDate lastAttendanceRewardDate) {
        this.attendanceStreak = attendanceStreak;
        this.lastAttendanceRewardDate = lastAttendanceRewardDate;

        if (this.attendanceStreak < 7) {
            this.keyCount += 1;
            return;
        }
        this.keyCount += 3;
    }
}
