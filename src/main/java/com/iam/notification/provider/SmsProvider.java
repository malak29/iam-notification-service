package com.iam.notification.provider;

import com.iam.notification.dto.SmsRequest;
import com.iam.notification.dto.NotificationResponse;
import reactor.core.publisher.Mono;

public interface SmsProvider {
    Mono<NotificationResponse> sendSms(SmsRequest request);
    Mono<Boolean> validatePhoneNumber(String phoneNumber);
    String getProviderName();
}
