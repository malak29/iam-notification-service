package com.iam.notification.provider;

import reactor.core.publisher.Mono;

public interface TemplateStorageProvider {
    Mono<String> getTemplate(String templateName, String language);
    Mono<Void> saveTemplate(String templateName, String language, String content);
    Mono<Boolean> templateExists(String templateName, String language);
    String getProviderName();
}