package com.iam.notification.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendBulkNotificationRequest {

    @NotEmpty(message = "Recipients list cannot be empty")
    private List<String> recipients;

    @NotBlank(message = "Template name is required")
    private String templateName;

    @NotBlank(message = "Notification type is required")
    private String notificationType; // EMAIL, SMS, BOTH

    private Map<String, Object> variables;

    @Builder.Default
    private String language = "en";

    private UUID requestedBy;
    private Integer organizationId;
}
