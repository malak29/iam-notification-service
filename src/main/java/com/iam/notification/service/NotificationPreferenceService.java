package com.iam.notification.service;

import com.iam.notification.dto.NotificationPreferenceRequest;
import com.iam.notification.model.UserNotificationPreference;
import com.iam.notification.repository.UserNotificationPreferenceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationPreferenceService {

    private final UserNotificationPreferenceRepository preferenceRepository;

    public Flux<UserNotificationPreference> getUserPreferences(UUID userId) {
        return preferenceRepository.findByUserId(userId)
                .switchIfEmpty(createDefaultPreferences(userId).thenMany(preferenceRepository.findByUserId(userId)));
    }

    public Mono<UserNotificationPreference> updatePreference(UUID userId, NotificationPreferenceRequest request) {
        return preferenceRepository.findByUserIdAndNotificationCategory(userId, request.getNotificationCategory())
                .switchIfEmpty(createSinglePreference(userId, request.getNotificationCategory()))
                .flatMap(preference -> {
                    if (request.getEmailEnabled() != null) {
                        preference.setEmailEnabled(request.getEmailEnabled());
                    }
                    if (request.getSmsEnabled() != null) {
                        preference.setSmsEnabled(request.getSmsEnabled());
                    }
                    if (request.getPushEnabled() != null) {
                        preference.setPushEnabled(request.getPushEnabled());
                    }
                    if (request.getInAppEnabled() != null) {
                        preference.setInAppEnabled(request.getInAppEnabled());
                    }
                    if (request.getFrequency() != null) {
                        preference.setFrequency(request.getFrequency());
                    }
                    preference.setUpdatedAt(LocalDateTime.now());

                    return preferenceRepository.save(preference);
                });
    }

    public Mono<Void> createDefaultPreferences(UUID userId) {
        log.info("Creating default notification preferences for user: {}", userId);

        return preferenceRepository.createDefaultPreferences(userId)
                .doOnSuccess(unused -> log.debug("Default preferences created for user: {}", userId));
    }

    private Mono<UserNotificationPreference> createSinglePreference(UUID userId, String category) {
        UserNotificationPreference preference = UserNotificationPreference.builder()
                .preferenceId(UUID.randomUUID())
                .userId(userId)
                .notificationCategory(category)
                .emailEnabled(true)
                .smsEnabled(false)
                .pushEnabled(true)
                .inAppEnabled(true)
                .frequency("IMMEDIATE")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        return preferenceRepository.save(preference);
    }
}