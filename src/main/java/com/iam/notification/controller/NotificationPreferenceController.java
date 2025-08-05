package com.iam.notification.controller;

import com.iam.notification.config.ApiRoutes;
import com.iam.notification.dto.NotificationPreferenceRequest;
import com.iam.notification.model.UserNotificationPreference;
import com.iam.notification.service.NotificationPreferenceService;
import com.iam.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(ApiRoutes.PREFERENCES)
@RequiredArgsConstructor
@Slf4j
public class NotificationPreferenceController {

    private final NotificationPreferenceService preferenceService;

    @GetMapping(ApiRoutes.USER_PREFERENCES)
    public Mono<ResponseEntity<ApiResponse<List<UserNotificationPreference>>>> getUserPreferences(@PathVariable UUID userId) {
        return preferenceService.getUserPreferences(userId)
                .collectList()
                .map(preferences -> ResponseEntity.ok(ApiResponse.success(preferences, "User preferences retrieved")));
    }

    @PutMapping(ApiRoutes.UPDATE_PREFERENCE)
    public Mono<ResponseEntity<ApiResponse<UserNotificationPreference>>> updatePreference(
            @PathVariable UUID userId,
            @Valid @RequestBody Mono<NotificationPreferenceRequest> requestMono) {

        return requestMono
                .flatMap(request -> preferenceService.updatePreference(userId, request))
                .map(preference -> ResponseEntity.ok(ApiResponse.success(preference, "Preference updated successfully")));
    }

    @PostMapping(ApiRoutes.CREATE_DEFAULT_PREFERENCES)
    public Mono<ResponseEntity<ApiResponse<Void>>> createDefaultPreferences(@PathVariable UUID userId) {
        return preferenceService.createDefaultPreferences(userId)
                .then(Mono.just(ResponseEntity.ok(ApiResponse.<Void>success("Default preferences created"))));
    }
}
