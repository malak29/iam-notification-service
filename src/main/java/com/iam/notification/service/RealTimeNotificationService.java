package com.iam.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class RealTimeNotificationService {

    private final Map<UUID, WebSocketSession> userSessions = new ConcurrentHashMap<>();

    public void addUserSession(UUID userId, WebSocketSession session) {
        userSessions.put(userId, session);
        log.info("User {} connected to real-time notifications", userId);
    }

    public void removeUserSession(UUID userId) {
        userSessions.remove(userId);
        log.info("User {} disconnected from real-time notifications", userId);
    }

    public Mono<Void> sendNotification(UUID userId, String message, String type) {
        WebSocketSession session = userSessions.get(userId);

        if (session == null || !session.isOpen()) {
            log.debug("No active session for user: {}", userId);
            return Mono.empty();
        }

        String notification = String.format("""
            {
                "type": "%s",
                "message": "%s",
                "timestamp": "%s",
                "userId": "%s"
            }
            """, type, message, java.time.LocalDateTime.now(), userId);

        return session.send(Mono.just(session.textMessage(notification)))
                .doOnSuccess(unused -> log.debug("Real-time notification sent to user: {}", userId))
                .onErrorResume(error -> {
                    log.warn("Failed to send real-time notification to user {}: {}", userId, error.getMessage());
                    removeUserSession(userId);
                    return Mono.empty();
                });
    }

    public Mono<Void> broadcastToAllUsers(String message, String type) {
        log.info("Broadcasting notification to {} connected users", userSessions.size());

        return Mono.fromRunnable(() -> {
            userSessions.entrySet().parallelStream()
                    .forEach(entry -> {
                        sendNotification(entry.getKey(), message, type)
                                .subscribe();
                    });
        });
    }

    public int getConnectedUserCount() {
        return userSessions.size();
    }

    public boolean isUserConnected(UUID userId) {
        WebSocketSession session = userSessions.get(userId);
        return session != null && session.isOpen();
    }
}