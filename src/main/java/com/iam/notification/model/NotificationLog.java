package com.iam.notification.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Table("notification_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationLog {

    @Id
    @Column("log_id")
    private UUID logId;

    @Column("notification_type")
    private String notificationType; // EMAIL, SMS, PUSH, IN_APP

    @Column("recipient")
    private String recipient; // email, phone, user_id

    @Column("template_name")
    private String templateName;

    @Column("subject")
    private String subject;

    @Column("content")
    private String content;

    @Column("status")
    private String status; // PENDING, SENT, FAILED, DELIVERED

    @Column("provider")
    private String provider; // SMTP, AWS_SES, AWS_SNS, TWILIO, SENDGRID

    @Column("provider_message_id")
    private String providerMessageId;

    @Column("error_message")
    private String errorMessage;

    @Column("sent_at")
    private LocalDateTime sentAt;

    @Column("delivered_at")
    private LocalDateTime deliveredAt;

    @Column("retry_count")
    @Builder.Default
    private Integer retryCount = 0;

    @Column("max_retries")
    @Builder.Default
    private Integer maxRetries = 3;

    @Column("user_id")
    private UUID userId;

    @Column("organization_id")
    private Integer organizationId;

    @CreatedDate
    @Column("created_at")
    private LocalDateTime createdAt;
}