package com.iam.notification.repository;

import com.iam.notification.model.NotificationTemplate;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface NotificationTemplateRepository extends ReactiveCrudRepository<NotificationTemplate, UUID> {

    // Basic queries
    Mono<NotificationTemplate> findByTemplateNameAndLanguage(String templateName, String language);
    Mono<NotificationTemplate> findByTemplateNameAndTemplateTypeAndLanguage(String templateName, String templateType, String language);
    Flux<NotificationTemplate> findByTemplateType(String templateType);
    Flux<NotificationTemplate> findByLanguage(String language);
    Flux<NotificationTemplate> findByIsActive(Boolean isActive);
    Flux<NotificationTemplate> findByIsSystemTemplate(Boolean isSystemTemplate);

    // Search queries
    @Query("SELECT * FROM notification_templates WHERE template_name ILIKE :search AND is_active = true")
    Flux<NotificationTemplate> searchByTemplateName(String search);

    @Query("SELECT * FROM notification_templates WHERE template_type = :type AND is_active = true")
    Flux<NotificationTemplate> findActiveByType(String type);

    // System templates
    @Query("SELECT * FROM notification_templates WHERE is_system_template = true AND is_active = true")
    Flux<NotificationTemplate> findSystemTemplates();

    // Language fallback
    @Query("SELECT * FROM notification_templates WHERE template_name = :templateName AND " +
            "(language = :language OR language = 'en') AND is_active = true ORDER BY " +
            "CASE WHEN language = :language THEN 1 ELSE 2 END LIMIT 1")
    Mono<NotificationTemplate> findWithLanguageFallback(String templateName, String language);
}
