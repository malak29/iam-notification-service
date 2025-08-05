package com.iam.notification.provider;

import com.iam.notification.dto.EmailRequest;
import com.iam.notification.dto.NotificationResponse;
import reactor.core.publisher.Mono;

public interface EmailProvider {
    Mono<NotificationResponse> sendEmail(EmailRequest request);
    Mono<Boolean> verifyEmailAddress(String email);
    String getProviderName();
}