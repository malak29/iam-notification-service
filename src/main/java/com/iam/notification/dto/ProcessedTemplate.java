package com.iam.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessedTemplate {
    private String subject;
    private String bodyHtml;
    private String bodyText;
    private String templateName;
    private String language;
}