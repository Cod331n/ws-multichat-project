package ru.geoderma.service.discord.handler;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.PongMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import ru.geoderma.service.discord.app.BridgeDiscordBot;
import ru.geoderma.service.discord.common.ChatMessage;
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
    private BridgeDiscordBot bot;

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
        }, 0, 10_000, TimeUnit.MILLISECONDS);
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
            if (bot != null) {
                bot.sendMessage(chatMessage);
            } else {
                log.warn("Bot is not set, cannot send message");
            }
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
        executorService.shutdown();

        if (session != null && session.isOpen()) {
            try {
                session.close();
            } catch (IOException e) {
                log.error("Error closing session", e);
            }
        }
    }

}