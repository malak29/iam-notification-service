package com.iam.notification.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendTemplatedEmailRequest {

    @Email(message = "Please provide a valid email address")
    @NotBlank(message = "Email is required")
    private String to;

    private String from;

    @NotBlank(message = "Template name is required")
    private String templateName;

    private Map<String, Object> variables;

    @Builder.Default
    private String language = "en";

    private UUID userId;
    private Integer organizationId;
}
