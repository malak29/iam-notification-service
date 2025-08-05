package com.iam.notification.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendEmailRequest {

    @Email(message = "Please provide a valid email address")
    @NotBlank(message = "Email is required")
    private String to;

    private String from;

    @NotBlank(message = "Subject is required")
    private String subject;

    private String bodyHtml;
    private String bodyText;
    private String replyTo;
    private UUID userId;
    private Integer organizationId;
}