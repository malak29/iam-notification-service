package com.iam.notification.service;

import com.iam.notification.dto.ProcessedTemplate;
import com.iam.notification.model.NotificationTemplate;
import com.iam.notification.provider.TemplateStorageProvider;
import com.iam.notification.repository.NotificationTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class TemplateService {

    private final NotificationTemplateRepository templateRepository;
    private final TemplateStorageProvider templateStorageProvider;
    private final ReactiveRedisTemplate<String, String> redisTemplate;
    private final TemplateEngine templateEngine;

    private static final String TEMPLATE_CACHE_KEY = "template:";
    private static final Duration CACHE_TTL = Duration.ofHours(1);

    public Mono<ProcessedTemplate> processEmailTemplate(String templateName, Map<String, Object> variables, String language) {
        log.debug("Processing email template: {} for language: {}", templateName, language);

        return getTemplate(templateName, "EMAIL", language)
                .flatMap(template -> processTemplate(template, variables))
                .doOnSuccess(processed -> log.debug("Template {} processed successfully", templateName))
                .onErrorResume(throwable -> {
                    log.error("Failed to process template {}: {}", templateName, throwable.getMessage());
                    return getDefaultErrorTemplate(templateName, language);
                });
    }

    public Mono<String> processSmsTemplate(String templateName, Map<String, Object> variables, String language) {
        return getTemplate(templateName, "SMS", language)
                .map(template -> processTextTemplate(template.getBodyText(), variables))
                .doOnSuccess(processed -> log.debug("SMS template {} processed successfully", templateName));
    }

    private Mono<NotificationTemplate> getTemplate(String templateName, String type, String language) {
        String cacheKey = TEMPLATE_CACHE_KEY + templateName + ":" + type + ":" + language;

        // Try cache first
        return redisTemplate.opsForValue().get(cacheKey)
                .cast(NotificationTemplate.class)
                .switchIfEmpty(
                        // Cache miss - get from database
                        templateRepository.findByTemplateNameAndTemplateTypeAndLanguage(templateName, type, language)
                                .switchIfEmpty(
                                        // Fallback to English if language-specific template not found
                                        templateRepository.findByTemplateNameAndTemplateTypeAndLanguage(templateName, type, "en")
                                                .doOnNext(template -> log.debug("Using English fallback for template: {}", templateName))
                                )
                                .switchIfEmpty(
                                        // Fallback to storage provider
                                        templateStorageProvider.getTemplate(templateName, language)
                                                .map(content -> NotificationTemplate.builder()
                                                        .templateName(templateName)
                                                        .templateType(type)
                                                        .bodyHtml(content)
                                                        .language(language)
                                                        .build())
                                )
                                .flatMap(template ->
                                        // Cache the result
                                        redisTemplate.opsForValue()
                                                .set(cacheKey, template.toString(), CACHE_TTL)
                                                .thenReturn(template)
                                )
                );
    }

    private Mono<ProcessedTemplate> processTemplate(NotificationTemplate template, Map<String, Object> variables) {
        return Mono.fromCallable(() -> {
            Context context = new Context();
            if (variables != null) {
                variables.forEach(context::setVariable);
            }

            String processedSubject = template.getSubject() != null ?
                    templateEngine.process(template.getSubject(), context) : "";

            String processedHtml = template.getBodyHtml() != null ?
                    templateEngine.process(template.getBodyHtml(), context) : "";

            String processedText = template.getBodyText() != null ?
                    templateEngine.process(template.getBodyText(), context) : "";

            return ProcessedTemplate.builder()
                    .subject(processedSubject)
                    .bodyHtml(processedHtml)
                    .bodyText(processedText)
                    .templateName(template.getTemplateName())
                    .language(template.getLanguage())
                    .build();
        });
    }

    private String processTextTemplate(String template, Map<String, Object> variables) {
        if (template == null || variables == null) {
            return template;
        }

        Context context = new Context();
        variables.forEach(context::setVariable);
        return templateEngine.process(template, context);
    }

    private Mono<ProcessedTemplate> getDefaultErrorTemplate(String templateName, String language) {
        return Mono.just(ProcessedTemplate.builder()
                .subject("Notification")
                .bodyHtml("<p>A notification was sent from IAM System.</p>")
                .bodyText("A notification was sent from IAM System.")
                .templateName(templateName)
                .language(language)
                .build());
    }
}