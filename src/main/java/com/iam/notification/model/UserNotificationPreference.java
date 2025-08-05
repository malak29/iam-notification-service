package com.iam.notification.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Table("user_notification_preferences")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserNotificationPreference {

    @Id
    @Column("preference_id")
    private UUID preferenceId;

    @Column("user_id")
    private UUID userId;

    @Column("notification_category")
    private String notificationCategory; // SECURITY, ACCOUNT, SYSTEM, MARKETING

    @Column("email_enabled")
    @Builder.Default
    private Boolean emailEnabled = true;

    @Column("sms_enabled")
    @Builder.Default
    private Boolean smsEnabled = false;

    @Column("push_enabled")
    @Builder.Default
    private Boolean pushEnabled = true;

    @Column("in_app_enabled")
    @Builder.Default
    private Boolean inAppEnabled = true;

    @Column("frequency")
    @Builder.Default
    private String frequency = "IMMEDIATE"; // IMMEDIATE, DAILY, WEEKLY, DISABLED

    @CreatedDate
    @Column("created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column("updated_at")
    private LocalDateTime updatedAt;
}