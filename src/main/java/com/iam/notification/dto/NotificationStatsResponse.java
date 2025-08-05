package com.iam.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationStatsResponse {
    private long totalSent;
    private long totalFailed;
    private long emailsSent;
    private long smsSent;
    private long realTimeNotifications;
    private double successRate;
    private LocalDateTime periodStart;
    private LocalDateTime periodEnd;
    private String mostUsedTemplate;
    private String primaryProvider;
}