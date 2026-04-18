package ru.geoderma.service.dispatcher.route;

import org.jetbrains.annotations.NotNull;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;

@FunctionalInterface
public interface MessageRouter {

    void route(@NotNull final TextMessage textMessage,
               @NotNull final WebSocketSession source,
               @NotNull final Map<String, WebSocketSession> sessions);

}
