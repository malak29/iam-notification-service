package com.iam.notification.repository;

import com.iam.notification.model.UserNotificationPreference;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface UserNotificationPreferenceRepository extends ReactiveCrudRepository<UserNotificationPreference, UUID> {

    // Basic queries
    Flux<UserNotificationPreference> findByUserId(UUID userId);
    Mono<UserNotificationPreference> findByUserIdAndNotificationCategory(UUID userId, String notificationCategory);

    // Preference checks
    @Query("SELECT email_enabled FROM user_notification_preferences WHERE user_id = :userId AND notification_category = :category")
    Mono<Boolean> isEmailEnabledForUser(UUID userId, String category);

    @Query("SELECT sms_enabled FROM user_notification_preferences WHERE user_id = :userId AND notification_category = :category")
    Mono<Boolean> isSmsEnabledForUser(UUID userId, String category);

    @Query("SELECT push_enabled FROM user_notification_preferences WHERE user_id = :userId AND notification_category = :category")
    Mono<Boolean> isPushEnabledForUser(UUID userId, String category);

    @Query("SELECT in_app_enabled FROM user_notification_preferences WHERE user_id = :userId AND notification_category = :category")
    Mono<Boolean> isInAppEnabledForUser(UUID userId, String category);

    // Bulk operations
    @Query("SELECT user_id FROM user_notification_preferences WHERE email_enabled = true AND notification_category = :category")
    Flux<UUID> findUsersWithEmailEnabled(String category);

    @Query("SELECT user_id FROM user_notification_preferences WHERE sms_enabled = true AND notification_category = :category")
    Flux<UUID> findUsersWithSmsEnabled(String category);

    // Update operations
    @Modifying
    @Query("UPDATE user_notification_preferences SET email_enabled = :enabled WHERE user_id = :userId AND notification_category = :category")
    Mono<Integer> updateEmailEnabled(UUID userId, String category, Boolean enabled);

    @Modifying
    @Query("UPDATE user_notification_preferences SET sms_enabled = :enabled WHERE user_id = :userId AND notification_category = :category")
    Mono<Integer> updateSmsEnabled(UUID userId, String category, Boolean enabled);

    // Default preferences creation
    @Query("INSERT INTO user_notification_preferences (preference_id, user_id, notification_category, email_enabled, sms_enabled, push_enabled, in_app_enabled, frequency, created_at, updated_at) " +
            "VALUES (gen_random_uuid(), :userId, 'ACCOUNT', true, false, true, true, 'IMMEDIATE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP), " +
            "(gen_random_uuid(), :userId, 'SECURITY', true, true, true, true, 'IMMEDIATE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP), " +
            "(gen_random_uuid(), :userId, 'SYSTEM', true, false, true, true, 'DAILY', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP), " +
            "(gen_random_uuid(), :userId, 'MARKETING', false, false, false, false, 'DISABLED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP) " +
            "ON CONFLICT (user_id, notification_category) DO NOTHING")
    Mono<Void> createDefaultPreferences(UUID userId);
}