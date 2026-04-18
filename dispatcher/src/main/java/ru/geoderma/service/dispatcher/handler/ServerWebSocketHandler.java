package ru.geoderma.service.dispatcher.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.PongMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import ru.geoderma.service.dispatcher.common.ClientConnectionMessage;
import ru.geoderma.service.dispatcher.route.MessageRouter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class ServerWebSocketHandler extends TextWebSocketHandler {

    private final Map<String, WebSocketSession> activeSessions = new ConcurrentHashMap<>();

    private final MessageRouter router;

    private final HeartbeatHandler heartbeatHandler;

    @Override
    public void handleTextMessage(final @NonNull WebSocketSession session,
                                  final @NonNull TextMessage message) {
        router.route(message, session, activeSessions);
        log.debug("Message received: {}", message);
    }

    @Override
    public void afterConnectionEstablished(final @NonNull WebSocketSession session) {
        ClientConnectionMessage connectionInfo = (ClientConnectionMessage) session.getAttributes().get("connection_info");
        activeSessions.put(connectionInfo.getBotId(), session);
    }

    @Override
    public void afterConnectionClosed(final WebSocketSession session,
                                      final @NonNull CloseStatus status) {
        ClientConnectionMessage connectionInfo = (ClientConnectionMessage) session.getAttributes().get("connection_info");

        if (connectionInfo != null) {
            activeSessions.remove(connectionInfo.getBotId());
        }
    }

    @Override
    protected void handlePongMessage(@NonNull WebSocketSession session, @NonNull PongMessage message) {
        session.getAttributes().put("last_heartbeat", System.currentTimeMillis());
        log.debug("Heartbeat received from {}", session.getId());
    }

    @Scheduled(fixedDelay = 30_000)
    private void heartbeat() {
        heartbeatHandler.heartbeat(activeSessions);
    }

    public void disconnect() {
        activeSessions.forEach((_, session) -> {
            try {
                session.close(CloseStatus.NORMAL);
            } catch (IOException e) {
                log.error("Error closing session", e);
            }
        });
        activeSessions.clear();
    }

}