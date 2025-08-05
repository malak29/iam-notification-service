package com.iam.notification.provider.aws;

import com.iam.notification.provider.EmailProvider;
import com.iam.notification.dto.EmailRequest;
import com.iam.notification.dto.NotificationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import software.amazon.awssdk.services.ses.SesAsyncClient;
import software.amazon.awssdk.services.ses.model.*;

import java.util.concurrent.CompletableFuture;

@Service
@ConditionalOnProperty(name = "notification.email.provider", havingValue = "aws-ses")
@RequiredArgsConstructor
@Slf4j
public class AwsSesEmailProvider implements EmailProvider {

    private final SesAsyncClient sesClient;

    @Override
    public Mono<NotificationResponse> sendEmail(EmailRequest request) {
        log.info("Sending email via AWS SES to: {}", request.getTo());

        return Mono.fromFuture(this::sendEmailAsync)
                .subscribeOn(Schedulers.boundedElastic())
                .map(response -> NotificationResponse.builder()
                        .success(true)
                        .provider("AWS_SES")
                        .providerId(response.messageId())
                        .message("Email sent successfully via AWS SES")
                        .build())
                .onErrorResume(throwable -> {
                    log.error("Failed to send email via AWS SES: {}", throwable.getMessage(), throwable);
                    return Mono.just(NotificationResponse.builder()
                            .success(false)
                            .provider("AWS_SES")
                            .errorMessage(throwable.getMessage())
                            .build());
                });
    }

    private CompletableFuture<SendEmailResponse> sendEmailAsync(EmailRequest request) {
        try {
            // Build email content
            Content subject = Content.builder()
                    .data(request.getSubject())
                    .charset("UTF-8")
                    .build();

            Content htmlBody = Content.builder()
                    .data(request.getBodyHtml())
                    .charset("UTF-8")
                    .build();

            Content textBody = Content.builder()
                    .data(request.getBodyText())
                    .charset("UTF-8")
                    .build();

            Body body = Body.builder()
                    .html(htmlBody)
                    .text(textBody)
                    .build();

            Message message = Message.builder()
                    .subject(subject)
                    .body(body)
                    .build();

            // Build destination
            Destination destination = Destination.builder()
                    .toAddresses(request.getTo())
                    .build();

            // Build send request
            SendEmailRequest sendRequest = SendEmailRequest.builder()
                    .source(request.getFrom())
                    .destination(destination)
                    .message(message)
                    .build();

            return sesClient.sendEmail(sendRequest);

        } catch (Exception e) {
            CompletableFuture<SendEmailResponse> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }

    @Override
    public Mono<Boolean> verifyEmailAddress(String email) {
        return Mono.fromFuture(() -> {
                    GetIdentityVerificationAttributesRequest request =
                            GetIdentityVerificationAttributesRequest.builder()
                                    .identities(email)
                                    .build();

                    return sesClient.getIdentityVerificationAttributes(request);
                })
                .map(response -> {
                    IdentityVerificationAttributes attrs = response.verificationAttributes().get(email);
                    return attrs != null && attrs.verificationStatus() == VerificationStatus.SUCCESS;
                })
                .onErrorReturn(false)
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public String getProviderName() {
        return "AWS_SES";
    }
}
