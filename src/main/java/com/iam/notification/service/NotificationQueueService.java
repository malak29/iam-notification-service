package com.iam.notification.service;

import com.iam.notification.dto.SendBulkNotificationRequest;
import com.iam.notification.model.NotificationLog;
import com.iam.notification.repository.NotificationLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationQueueService {

    private final ReactiveRedisTemplate<String, String> redisTemplate;
    private final NotificationLogRepository logRepository;
    private final NotificationService notificationService;

    private static final String QUEUE_KEY = "notification:queue";
    private static final String RETRY_QUEUE_KEY = "notification:retry";

    public Mono<Void> enqueueBulkNotification(SendBulkNotificationRequest request) {
        log.info("Queuing bulk notification for {} recipients", request.getRecipients().size());

        return Flux.fromIterable(request.getRecipients())
                .flatMap(recipient -> {
                    String queueItem = String.format("%s|%s|%s|%s",
                            recipient,
                            request.getTemplateName(),
                            request.getNotificationType(),
                            request.getRequestedBy());

                    return redisTemplate.opsForList().leftPush(QUEUE_KEY, queueItem);
                })
                .then()
                .doOnSuccess(unused -> log.info("Bulk notification queued successfully"));
    }

    public Mono<Void> enqueueRetry(NotificationLog failedLog) {
        if (failedLog.getRetryCount() >= failedLog.getMaxRetries()) {
            log.warn("Max retries exceeded for notification: {}", failedLog.getLogId());
            return Mono.empty();
        }

        String retryItem = String.format("%s|%d", failedLog.getLogId(), failedLog.getRetryCount() + 1);

        return redisTemplate.opsForList()
                .leftPush(RETRY_QUEUE_KEY, retryItem)
                .then()
                .doOnSuccess(unused -> log.debug("Notification {} queued for retry", failedLog.getLogId()));
    }

    @Scheduled(fixedDelay = 5000) // Process queue every 5 seconds
    public void processQueue() {
        redisTemplate.opsForList()
                .rightPop(QUEUE_KEY)
                .repeatWhenEmpty(flux -> flux.delayElements(java.time.Duration.ofSeconds(1)))
                .take(java.time.Duration.ofSeconds(4)) // Process for 4 seconds
                .flatMap(this::processQueueItem)
                .subscribe(
                        unused -> {},
                        error -> log.error("Error processing queue: {}", error.getMessage()),
                        () -> log.trace("Queue processing cycle completed")
                );
    }

    @Scheduled(fixedDelay = 30000) // Process retry queue every 30 seconds
    public void processRetryQueue() {
        redisTemplate.opsForList()
                .rightPop(RETRY_QUEUE_KEY)
                .repeatWhenEmpty(flux -> flux.delayElements(java.time.Duration.ofSeconds(5)))
                .take(java.time.Duration.ofSeconds(25))
                .flatMap(this::processRetryItem)
                .subscribe(
                        unused -> {},
                        error -> log.error("Error processing retry queue: {}", error.getMessage()),
                        () -> log.trace("Retry queue processing cycle completed")
                );
    }

    private Mono<Void> processQueueItem(String queueItem) {
        String[] parts = queueItem.split("\\|");
        if (parts.length != 4) {
            log.warn("Invalid queue item format: {}", queueItem);
            return Mono.empty();
        }

        String recipient = parts[0];
        String templateName = parts[1];
        String notificationType = parts[2];
        UUID requestedBy = UUID.fromString(parts[3]);

        log.debug("Processing queued notification: {} to {}", templateName, recipient);

        // Process based on notification type
        if ("EMAIL".equals(notificationType)) {
            return processQueuedEmail(recipient, templateName, requestedBy);
        } else if ("SMS".equals(notificationType)) {
            return processQueuedSms(recipient, templateName, requestedBy);
        }

        return Mono.empty();
    }

    private Mono<Void> processRetryItem(String retryItem) {
        String[] parts = retryItem.split("\\|");
        if (parts.length != 2) {
            log.warn("Invalid retry item format: {}", retryItem);
            return Mono.empty();
        }

        UUID logId = UUID.fromString(parts[0]);
        int retryCount = Integer.parseInt(parts[1]);

        return logRepository.findById(logId)
                .flatMap(log -> {
                    log.setRetryCount(retryCount);
                    log.setStatus("PENDING");

                    // Retry the notification based on type
                    if ("EMAIL".equals(log.getNotificationType())) {
                        return retryEmailNotification(log);
                    } else if ("SMS".equals(log.getNotificationType())) {
                        return retrySmsNotification(log);
                    }

                    return Mono.empty();
                })
                .then();
    }

    private Mono<Void> processQueuedEmail(String recipient, String templateName, UUID requestedBy) {
        // Implementation for queued email processing
        return Mono.empty(); // Placeholder
    }

    private Mono<Void> processQueuedSms(String recipient, String templateName, UUID requestedBy) {
        // Implementation for queued SMS processing
        return Mono.empty(); // Placeholder
    }

    private Mono<Void> retryEmailNotification(NotificationLog log) {
        // Implementation for email retry
        return Mono.empty(); // Placeholder
    }

    private Mono<Void> retrySmsNotification(NotificationLog log) {
        // Implementation for SMS retry
        return Mono.empty(); // Placeholder
    }
}