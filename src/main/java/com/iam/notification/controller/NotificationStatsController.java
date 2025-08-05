package com.iam.notification.controller;

import com.iam.notification.config.ApiRoutes;
import com.iam.notification.dto.NotificationStatsResponse;
import com.iam.notification.service.NotificationStatsService;
import com.iam.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@RestController
@RequestMapping(ApiRoutes.STATS)
@RequiredArgsConstructor
public class NotificationStatsController {

    private final NotificationStatsService statsService;

    @GetMapping(ApiRoutes.DAILY_STATS)
    public Mono<ResponseEntity<ApiResponse<NotificationStatsResponse>>> getDailyStats() {
        return statsService.getDailyStats()
                .map(stats -> ResponseEntity.ok(ApiResponse.success(stats, "Daily stats retrieved")));
    }

    @GetMapping(ApiRoutes.WEEKLY_STATS)
    public Mono<ResponseEntity<ApiResponse<NotificationStatsResponse>>> getWeeklyStats() {
        return statsService.getWeeklyStats()
                .map(stats -> ResponseEntity.ok(ApiResponse.success(stats, "Weekly stats retrieved")));
    }

    @GetMapping(ApiRoutes.CUSTOM_STATS)
    public Mono<ResponseEntity<ApiResponse<NotificationStatsResponse>>> getCustomStats(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {

        return statsService.getStats(start, end)
                .map(stats -> ResponseEntity.ok(ApiResponse.success(stats, "Custom stats retrieved")));
    }
}