package com.iam.notification.service;

import com.iam.notification.dto.*;
import com.iam.notification.model.NotificationLog;
import com.iam.notification.provider.EmailProvider;
import com.iam.notification.provider.SmsProvider;
import com.iam.notification.repository.NotificationLogRepository;
import com.iam.notification.repository.UserNotificationPreferenceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final EmailProvider emailProvider;
    private final SmsProvider smsProvider;
    private final TemplateService templateService;
    private final NotificationLogRepository logRepository;
    private final UserNotificationPreferenceRepository preferenceRepository;
    private final NotificationQueueService queueService;
    private final RealTimeNotificationService realTimeService;

    public Mono<NotificationResponse> sendEmail(SendEmailRequest request) {
        log.info("Processing email notification to: {}", request.getTo());

        return checkUserEmailPreference(request.getUserId())
                .flatMap(enabled -> {
                    if (!enabled) {
                        return Mono.just(NotificationResponse.builder()
                                .success(false)
                                .message("Email notifications disabled for user")
                                .build());
                    }

                    return processEmailNotification(request);
                });
    }

    public Mono<NotificationResponse> sendSms(SendSmsRequest request) {
        log.info("Processing SMS notification to: {}", request.getPhoneNumber());

        return checkUserSmsPreference(request.getUserId())
                .flatMap(enabled -> {
                    if (!enabled) {
                        return Mono.just(NotificationResponse.builder()
                                .success(false)
                                .message("SMS notifications disabled for user")
                                .build());
                    }

                    return processSmsNotification(request);
                });
    }

    public Mono<NotificationResponse> sendTemplatedEmail(SendTemplatedEmailRequest request) {
        log.info("Processing templated email {} to: {}", request.getTemplateName(), request.getTo());

        return templateService.processEmailTemplate(
                        request.getTemplateName(),
                        request.getVariables(),
                        request.getLanguage()
                )
                .flatMap(processedTemplate -> {
                    EmailRequest emailRequest = EmailRequest.builder()
                            .to(request.getTo())
                            .from(request.getFrom())
                            .subject(processedTemplate.getSubject())
                            .bodyHtml(processedTemplate.getBodyHtml())
                            .bodyText(processedTemplate.getBodyText())
                            .build();

                    return emailProvider.sendEmail(emailRequest);
                })
                .flatMap(response -> logNotification(
                        "EMAIL",
                        request.getTo(),
                        request.getTemplateName(),
                        response,
                        request.getUserId()
                ).thenReturn(response));
    }

    public Mono<NotificationResponse> sendBulkNotification(SendBulkNotificationRequest request) {
        log.info("Processing bulk notification to {} recipients", request.getRecipients().size());

        return queueService.enqueueBulkNotification(request)
                .then(Mono.just(NotificationResponse.builder()
                        .success(true)
                        .message("Bulk notification queued successfully")
                        .build()));
    }

    public Mono<NotificationResponse> sendRealTimeNotification(SendRealTimeNotificationRequest request) {
        return realTimeService.sendNotification(request.getUserId(), request.getMessage(), request.getType())
                .then(Mono.just(NotificationResponse.builder()
                        .success(true)
                        .provider("WEBSOCKET")
                        .message("Real-time notification sent")
                        .build()));
    }

    // Integration methods for other services
    public Mono<Void> sendWelcomeEmail(UUID userId, String email, String name) {
        SendTemplatedEmailRequest request = SendTemplatedEmailRequest.builder()
                .to(email)
                .templateName("welcome")
                .variables(Map.of(
                        "name", name,
                        "loginUrl", "https://iam-system.com/login",
                        "supportEmail", "support@iam-system.com"
                ))
                .userId(userId)
                .build();

        return sendTemplatedEmail(request).then();
    }

    public Mono<Void> sendPasswordResetEmail(UUID userId, String email, String resetToken) {
        SendTemplatedEmailRequest request = SendTemplatedEmailRequest.builder()
                .to(email)
                .templateName("password-reset")
                .variables(Map.of(
                        "resetUrl", "https://iam-system.com/reset-password?token=" + resetToken,
                        "expiryHours", "1"
                ))
                .userId(userId)
                .build();

        return sendTemplatedEmail(request).then();
    }

    public Mono<Void> sendSecurityAlert(UUID userId, String email, String phoneNumber, String alertType) {
        // Send both email and SMS for security alerts
        Mono<Void> emailAlert = sendTemplatedEmail(SendTemplatedEmailRequest.builder()
                .to(email)
                .templateName("security-alert")
                .variables(Map.of(
                        "alertType", alertType,
                        "timestamp", LocalDateTime.now().toString(),
                        "actionUrl", "https://iam-system.com/security"
                ))
                .userId(userId)
                .build()).then();

        Mono<Void> smsAlert = sendSms(SendSmsRequest.builder()
                .phoneNumber(phoneNumber)
                .message("Security Alert: " + alertType + ". Check your email for details.")
                .userId(userId)
                .build()).then();

        return Mono.when(emailAlert, smsAlert);
    }

    public Mono<Void> sendRoleChangeNotification(UUID userId, String email, String roleName, String action) {
        // Real-time notification
        Mono<Void> realTime = sendRealTimeNotification(SendRealTimeNotificationRequest.builder()
                .userId(userId)
                .type("role_change")
                .message(String.format("Your role has been %s: %s", action, roleName))
                .build()).then();

        // Email notification
        Mono<Void> emailNotification = sendTemplatedEmail(SendTemplatedEmailRequest.builder()
                .to(email)
                .templateName("role-change")
                .variables(Map.of(
                        "roleName", roleName,
                        "action", action,
                        "timestamp", LocalDateTime.now().toString()
                ))
                .userId(userId)
                .build()).then();

        return Mono.when(realTime, emailNotification);
    }

    // Helper methods
    private Mono<NotificationResponse> processEmailNotification(SendEmailRequest request) {
        EmailRequest emailRequest = EmailRequest.builder()
                .to(request.getTo())
                .from(request.getFrom())
                .subject(request.getSubject())
                .bodyHtml(request.getBodyHtml())
                .bodyText(request.getBodyText())
                .build();

        return emailProvider.sendEmail(emailRequest)
                .flatMap(response -> logNotification(
                        "EMAIL",
                        request.getTo(),
                        "direct",
                        response,
                        request.getUserId()
                ).thenReturn(response));
    }

    private Mono<NotificationResponse> processSmsNotification(SendSmsRequest request) {
        SmsRequest smsRequest = SmsRequest.builder()
                .phoneNumber(request.getPhoneNumber())
                .message(request.getMessage())
                .build();

        return smsProvider.sendSms(smsRequest)
                .flatMap(response -> logNotification(
                        "SMS",
                        request.getPhoneNumber(),
                        "direct",
                        response,
                        request.getUserId()
                ).thenReturn(response));
    }

    private Mono<Boolean> checkUserEmailPreference(UUID userId) {
        if (userId == null) return Mono.just(true);

        return preferenceRepository.findByUserIdAndNotificationCategory(userId, "ACCOUNT")
                .map(pref -> pref.getEmailEnabled())
                .defaultIfEmpty(true);
    }

    private Mono<Boolean> checkUserSmsPreference(UUID userId) {
        if (userId == null) return Mono.just(true);

        return preferenceRepository.findByUserIdAndNotificationCategory(userId, "ACCOUNT")
                .map(pref -> pref.getSmsEnabled())
                .defaultIfEmpty(false);
    }

    private Mono<Void> logNotification(String type, String recipient, String templateName,
                                       NotificationResponse response, UUID userId) {
        NotificationLog log = NotificationLog.builder()
                .logId(UUID.randomUUID())
                .notificationType(type)
                .recipient(recipient)
                .templateName(templateName)
                .status(response.isSuccess() ? "SENT" : "FAILED")
                .provider(response.getProvider())
                .providerMessageId(response.getProviderId())
                .errorMessage(response.getErrorMessage())
                .sentAt(response.isSuccess() ? LocalDateTime.now() : null)
                .userId(userId)
                .retryCount(0)
                .createdAt(LocalDateTime.now())
                .build();

        return logRepository.save(log).then();
    }
}