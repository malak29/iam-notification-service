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

@Table("notification_templates")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationTemplate {

    @Id
    @Column("template_id")
    private UUID templateId;

    @Column("template_name")
    private String templateName;

    @Column("template_type")
    private String templateType; // EMAIL, SMS, PUSH, IN_APP

    @Column("subject")
    private String subject;

    @Column("body_html")
    private String bodyHtml;

    @Column("body_text")
    private String bodyText;

    @Column("variables")
    private String variables; // JSON string of available variables

    @Column("language")
    @Builder.Default
    private String language = "en";

    @Column("is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column("is_system_template")
    @Builder.Default
    private Boolean isSystemTemplate = false;

    @CreatedDate
    @Column("created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column("updated_at")
    private LocalDateTime updatedAt;

    @Column("created_by")
    private UUID createdBy;

    @Column("updated_by")
    private UUID updatedBy;
}