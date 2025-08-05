package com.iam.notification.service;

import com.iam.notification.dto.NotificationStatsResponse;
import com.iam.notification.repository.NotificationLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationStatsService {

    private final NotificationLogRepository logRepository;

    public Mono<NotificationStatsResponse> getStats(LocalDateTime start, LocalDateTime end) {
        return Mono.zip(
                logRepository.countSuccessfulNotificationsSince(start),
                logRepository.countFailedNotificationsSince(start),
                logRepository.countByTypeAndCreatedAtAfter("EMAIL", start),
                logRepository.countByTypeAndCreatedAtAfter("SMS", start),
                logRepository.findMostUsedTemplateSince(start)
        ).map(tuple -> {
            long totalSent = tuple.getT1();
            long totalFailed = tuple.getT2();
            long emailsSent = tuple.getT3();
            long smsSent = tuple.getT4();
            String mostUsedTemplate = tuple.getT5();

            double successRate = totalSent + totalFailed > 0 ?
                    (double) totalSent / (totalSent + totalFailed) * 100 : 0;

            return NotificationStatsResponse.builder()
                    .totalSent(totalSent)
                    .totalFailed(totalFailed)
                    .emailsSent(emailsSent)
                    .smsSent(smsSent)
                    .realTimeNotifications(0) // Would come from real-time service
                    .successRate(successRate)
                    .periodStart(start)
                    .periodEnd(end)
                    .mostUsedTemplate(mostUsedTemplate)
                    .primaryProvider("MIXED") // Would be calculated
                    .build();
        });
    }

    public Mono<NotificationStatsResponse> getDailyStats() {
        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now();
        return getStats(start, end);
    }

    public Mono<NotificationStatsResponse> getWeeklyStats() {
        LocalDateTime start = LocalDateTime.now().minusWeeks(1);
        LocalDateTime end = LocalDateTime.now();
        return getStats(start, end);
    }
}