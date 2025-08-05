package com.iam.notification.controller;

import com.iam.notification.config.ApiRoutes;
import com.iam.notification.dto.*;
import com.iam.notification.service.NotificationService;
import com.iam.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping(ApiRoutes.NOTIFICATIONS)
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping(ApiRoutes.SEND_EMAIL)
    public Mono<ResponseEntity<ApiResponse<NotificationResponse>>> sendEmail(@Valid @RequestBody Mono<SendEmailRequest> requestMono) {
        return requestMono
                .doOnNext(request -> log.info("Email notification request for: {}", request.getTo()))
                .flatMap(notificationService::sendEmail)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Email notification processed")));
    }

    @PostMapping(ApiRoutes.SEND_SMS)
    public Mono<ResponseEntity<ApiResponse<NotificationResponse>>> sendSms(@Valid @RequestBody Mono<SendSmsRequest> requestMono) {
        return requestMono
                .doOnNext(request -> log.info("SMS notification request for: {}", request.getPhoneNumber()))
                .flatMap(notificationService::sendSms)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "SMS notification processed")));
    }

    @PostMapping(ApiRoutes.SEND_TEMPLATED_EMAIL)
    public Mono<ResponseEntity<ApiResponse<NotificationResponse>>> sendTemplatedEmail(@Valid @RequestBody Mono<SendTemplatedEmailRequest> requestMono) {
        return requestMono
                .doOnNext(request -> log.info("Templated email request: {} for {}", request.getTemplateName(), request.getTo()))
                .flatMap(notificationService::sendTemplatedEmail)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Templated email processed")));
    }

    @PostMapping(ApiRoutes.SEND_BULK)
    public Mono<ResponseEntity<ApiResponse<NotificationResponse>>> sendBulkNotification(@Valid @RequestBody Mono<SendBulkNotificationRequest> requestMono) {
        return requestMono
                .doOnNext(request -> log.info("Bulk notification request for {} recipients", request.getRecipients().size()))
                .flatMap(notificationService::sendBulkNotification)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Bulk notification queued")));
    }

    @PostMapping(ApiRoutes.SEND_REAL_TIME)
    public Mono<ResponseEntity<ApiResponse<NotificationResponse>>> sendRealTimeNotification(@Valid @RequestBody Mono<SendRealTimeNotificationRequest> requestMono) {
        return requestMono
                .flatMap(notificationService::sendRealTimeNotification)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Real-time notification sent")));
    }

    // Integration endpoints for other services
    @PostMapping(ApiRoutes.SEND_WELCOME_EMAIL)
    public Mono<ResponseEntity<ApiResponse<Void>>> sendWelcomeEmail(
            @RequestParam UUID userId,
            @RequestParam String email,
            @RequestParam String name) {

        return notificationService.sendWelcomeEmail(userId, email, name)
                .then(Mono.just(ResponseEntity.ok(ApiResponse.<Void>success("Welcome email sent"))));
    }

    @PostMapping(ApiRoutes.SEND_PASSWORD_RESET)
    public Mono<ResponseEntity<ApiResponse<Void>>> sendPasswordResetEmail(
            @RequestParam UUID userId,
            @RequestParam String email,
            @RequestParam String resetToken) {

        return notificationService.sendPasswordResetEmail(userId, email, resetToken)
                .then(Mono.just(ResponseEntity.ok(ApiResponse.<Void>success("Password reset email sent"))));
    }

    @PostMapping(ApiRoutes.SEND_SECURITY_ALERT)
    public Mono<ResponseEntity<ApiResponse<Void>>> sendSecurityAlert(
            @RequestParam UUID userId,
            @RequestParam String email,
            @RequestParam(required = false) String phoneNumber,
            @RequestParam String alertType) {

        return notificationService.sendSecurityAlert(userId, email, phoneNumber, alertType)
                .then(Mono.just(ResponseEntity.ok(ApiResponse.<Void>success("Security alert sent"))));
    }

    @PostMapping(ApiRoutes.SEND_ROLE_CHANGE)
    public Mono<ResponseEntity<ApiResponse<Void>>> sendRoleChangeNotification(
            @RequestParam UUID userId,
            @RequestParam String email,
            @RequestParam String roleName,
            @RequestParam String action) {

        return notificationService.sendRoleChangeNotification(userId, email, roleName, action)
                .then(Mono.just(ResponseEntity.ok(ApiResponse.<Void>success("Role change notification sent"))));
    }

    @GetMapping(ApiRoutes.HEALTH)
    public Mono<ResponseEntity<ApiResponse<String>>> health() {
        return Mono.just(ResponseEntity.ok(ApiResponse.success("OK", "Notification service is running")));
    }
}
