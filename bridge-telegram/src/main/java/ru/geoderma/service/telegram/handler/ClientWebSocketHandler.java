package ru.geoderma.service.telegram.handler;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import ru.geoderma.service.telegram.app.TelegramBot;
import ru.geoderma.service.telegram.common.ChatMessage;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class ClientWebSocketHandler extends AbstractClientWebSocketHandler {

    private final ObjectMapper objectMapper;

    @Setter
    private TelegramBot bot;

    private WebSocketSession session;

    private ScheduledExecutorService executorService;

    protected void startHeartbeat(final @NotNull WebSocketSession session) {
        this.executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(() -> {
            if (session != null && session.isOpen()) {
                try {
                    session.sendMessage(new PongMessage());
                    log.debug("Heartbeat sent by {}", session.getId());
                } catch (IOException e) {
                    log.warn("Failed to send heartbeat: {}", e.getLocalizedMessage());
                }
            }
        }, 0,10_000, TimeUnit.MILLISECONDS);
    }

    @Override
    public void afterConnectionEstablished(final @NonNull WebSocketSession session) {
        this.session = session;
        log.info("Connected to WebSocket server [{}]", session.getRemoteAddress() != null
                ? session.getRemoteAddress().toString()
                : "NULL");

        startHeartbeat(session);
    }



    @Override
    public void handleTextMessage(final @NonNull WebSocketSession session, final @NonNull TextMessage message) {
        try {
            String payload = message.getPayload();
            ChatMessage chatMessage = objectMapper.readValue(payload, ChatMessage.class);
            bot.sendMessage(chatMessage);
        } catch (Exception e) {
            log.error("Failed to process message: {}", e.getLocalizedMessage());
        }
    }

    @Override
    public void handleTransportError(final @NonNull WebSocketSession session, final @NonNull Throwable exception) {
        log.error("Transport error", exception);
        disconnect();
    }

    @Override
    public void afterConnectionClosed(final @NonNull WebSocketSession session, final @NonNull CloseStatus status) {
        log.info("WebSocket connection closed: {}", status);
        disconnect();
    }

    @Override
    public void sendMessage(final @NotNull ChatMessage message) {
        if (session == null || !session.isOpen()) {
            log.warn("Cannot send message: session is not open");
            return;
        }

        try {
            String jsonMessage = objectMapper.writeValueAsString(message);
            session.sendMessage(new TextMessage(jsonMessage));
            log.debug("Message sent: {}", jsonMessage);
        } catch (IOException e) {
            log.error("Failed to send message", e);
        }
    }

    @Override
    public void disconnect() {
        executorService.close();

        if (session != null && session.isOpen()) {
            try {
                session.close();
            } catch (IOException e) {
                log.error("Error closing session", e);
            }
        }
    }

}