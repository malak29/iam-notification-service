package com.iam.notification.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationPreferenceRequest {

    @NotNull(message = "User ID is required")
    private UUID userId;

    private String notificationCategory; // SECURITY, ACCOUNT, SYSTEM, MARKETING
    private Boolean emailEnabled;
    private Boolean smsEnabled;
    private Boolean pushEnabled;
    private Boolean inAppEnabled;
    private String frequency; // IMMEDIATE, DAILY, WEEKLY, DISABLED
}