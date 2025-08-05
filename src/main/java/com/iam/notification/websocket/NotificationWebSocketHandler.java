package com.iam.notification.websocket;

import com.iam.notification.service.RealTimeNotificationService;
import com.iam.common.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationWebSocketHandler implements WebSocketHandler {

    private final RealTimeNotificationService realTimeService;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        log.info("WebSocket connection attempt from: {}", session.getRemoteAddress());

        return authenticateSession(session)
                .flatMap(userId -> {
                    log.info("User {} connected to real-time notifications", userId);
                    realTimeService.addUserSession(userId, session);

                    return session.receive()
                            .doOnNext(message -> log.debug("Received WebSocket message from user {}: {}", userId, message.getPayloadAsText()))
                            .then()
                            .doFinally(signalType -> {
                                log.info("User {} disconnected from real-time notifications", userId);
                                realTimeService.removeUserSession(userId);
                            });
                })
                .onErrorResume(error -> {
                    log.warn("WebSocket authentication failed: {}", error.getMessage());
                    return session.close();
                });
    }

    private Mono<UUID> authenticateSession(WebSocketSession session) {
        URI uri = session.getHandshakeInfo().getUri();
        String query = uri.getQuery();

        if (query == null) {
            return Mono.error(new RuntimeException("No query parameters provided"));
        }

        // Extract token from query parameter: ?token=jwt_token
        String token = null;
        for (String param : query.split("&")) {
            String[] keyValue = param.split("=");
            if (keyValue.length == 2 && "token".equals(keyValue[0])) {
                token = keyValue[1];
                break;
            }
        }

        if (token == null) {
            return Mono.error(new RuntimeException("Token not provided"));
        }

        // Validate JWT token and extract user ID
        return jwtTokenProvider.validateToken(token)
                .map(claims -> UUID.fromString(claims.getSubject()))
                .onErrorMap(error -> new RuntimeException("Invalid token: " + error.getMessage()));
    }
}