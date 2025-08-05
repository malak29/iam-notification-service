package com.iam.notification.provider.aws;

import com.iam.notification.provider.SmsProvider;
import com.iam.notification.dto.SmsRequest;
import com.iam.notification.dto.NotificationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import software.amazon.awssdk.services.sns.SnsAsyncClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;

import java.util.concurrent.CompletableFuture;

@Service
@ConditionalOnProperty(name = "notification.sms.provider", havingValue = "aws-sns")
@RequiredArgsConstructor
@Slf4j
public class AwsSnsProvider implements SmsProvider {

    private final SnsAsyncClient snsClient;

    @Override
    public Mono<NotificationResponse> sendSms(SmsRequest request) {
        log.info("Sending SMS via AWS SNS to: {}", request.getPhoneNumber());

        return Mono.fromFuture(() -> sendSmsAsync(request))
                .subscribeOn(Schedulers.boundedElastic())
                .map(response -> NotificationResponse.builder()
                        .success(true)
                        .provider("AWS_SNS")
                        .providerId(response.messageId())
                        .message("SMS sent successfully via AWS SNS")
                        .build())
                .onErrorResume(throwable -> {
                    log.error("Failed to send SMS via AWS SNS: {}", throwable.getMessage(), throwable);
                    return Mono.just(NotificationResponse.builder()
                            .success(false)
                            .provider("AWS_SNS")
                            .errorMessage(throwable.getMessage())
                            .build());
                });
    }

    private CompletableFuture<PublishResponse> sendSmsAsync(SmsRequest request) {
        try {
            PublishRequest publishRequest = PublishRequest.builder()
                    .phoneNumber(formatPhoneNumber(request.getPhoneNumber()))
                    .message(request.getMessage())
                    .build();

            return snsClient.publish(publishRequest);

        } catch (Exception e) {
            CompletableFuture<PublishResponse> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }

    @Override
    public Mono<Boolean> validatePhoneNumber(String phoneNumber) {
        // AWS SNS validation logic
        return Mono.fromCallable(() -> {
            String formatted = formatPhoneNumber(phoneNumber);
            return formatted.matches("^\\+[1-9]\\d{1,14}$");
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public String getProviderName() {
        return "AWS_SNS";
    }

    private String formatPhoneNumber(String phoneNumber) {
        // Remove all non-digit characters except +
        String cleaned = phoneNumber.replaceAll("[^+\\d]", "");

        // Add + if not present
        if (!cleaned.startsWith("+")) {
            cleaned = "+" + cleaned;
        }

        return cleaned;
    }
}