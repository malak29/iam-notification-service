package com.iam.notification.provider.mock;

import com.iam.notification.provider.SmsProvider;
import com.iam.notification.dto.SmsRequest;
import com.iam.notification.dto.NotificationResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
@ConditionalOnProperty(name = "notification.sms.provider", havingValue = "mock", matchIfMissing = true)
@Slf4j
public class MockSmsProvider implements SmsProvider {

    @Override
    public Mono<NotificationResponse> sendSms(SmsRequest request) {
        log.info("🔔 MOCK SMS to {}: {}", request.getPhoneNumber(), request.getMessage());

        // Simulate processing time
        return Mono.delay(java.time.Duration.ofMillis(100))
                .then(Mono.just(NotificationResponse.builder()
                        .success(true)
                        .provider("MOCK_SMS")
                        .providerId(UUID.randomUUID().toString())
                        .message("SMS sent successfully (MOCK)")
                        .build()));
    }

    @Override
    public Mono<Boolean> validatePhoneNumber(String phoneNumber) {
        return Mono.fromCallable(() -> {
            String cleaned = phoneNumber.replaceAll("[^+\\d]", "");
            return cleaned.matches("^\\+[1-9]\\d{1,14}$");
        });
    }

    @Override
    public String getProviderName() {
        return "MOCK_SMS";
    }
}