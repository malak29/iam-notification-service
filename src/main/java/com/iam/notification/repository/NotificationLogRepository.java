package com.iam.notification.repository;

import com.iam.notification.model.NotificationLog;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface NotificationLogRepository extends ReactiveCrudRepository<NotificationLog, UUID> {

    // Basic queries
    Flux<NotificationLog> findByUserId(UUID userId);
    Flux<NotificationLog> findByNotificationType(String notificationType);
    Flux<NotificationLog> findByStatus(String status);
    Flux<NotificationLog> findByProvider(String provider);
    Flux<NotificationLog> findByTemplateName(String templateName);

    // Date range queries
    Flux<NotificationLog> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    Flux<NotificationLog> findBySentAtBetween(LocalDateTime start, LocalDateTime end);

    // User-specific queries
    Flux<NotificationLog> findByUserIdAndNotificationType(UUID userId, String notificationType);
    Flux<NotificationLog> findByUserIdAndStatus(UUID userId, String status);
    Flux<NotificationLog> findByUserIdAndCreatedAtBetween(UUID userId, LocalDateTime start, LocalDateTime end);

    // Failed notifications (for retry)
    @Query("SELECT * FROM notification_logs WHERE status = 'FAILED' AND retry_count < max_retries")
    Flux<NotificationLog> findFailedNotificationsForRetry();

    @Query("SELECT * FROM notification_logs WHERE status = 'PENDING' AND created_at < :cutoff")
    Flux<NotificationLog> findStuckNotifications(LocalDateTime cutoff);

    // Statistics queries
    @Query("SELECT COUNT(*) FROM notification_logs WHERE status = 'SENT' AND created_at >= :since")
    Mono<Long> countSuccessfulNotificationsSince(LocalDateTime since);

    @Query("SELECT COUNT(*) FROM notification_logs WHERE status = 'FAILED' AND created_at >= :since")
    Mono<Long> countFailedNotificationsSince(LocalDateTime since);

    @Query("SELECT COUNT(*) FROM notification_logs WHERE notification_type = :type AND created_at >= :since")
    Mono<Long> countByTypeAndCreatedAtAfter(String type, LocalDateTime since);

    @Query("SELECT template_name, COUNT(*) as usage_count FROM notification_logs " +
            "WHERE created_at >= :since GROUP BY template_name ORDER BY usage_count DESC LIMIT 1")
    Mono<String> findMostUsedTemplateSince(LocalDateTime since);

    // Organization queries
    Flux<NotificationLog> findByOrganizationId(Integer organizationId);
    Flux<NotificationLog> findByOrganizationIdAndCreatedAtBetween(Integer organizationId, LocalDateTime start, LocalDateTime end);
}