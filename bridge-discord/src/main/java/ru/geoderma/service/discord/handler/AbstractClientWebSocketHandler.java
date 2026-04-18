package ru.geoderma.service.discord.handler;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import ru.geoderma.service.discord.common.ChatMessage;

@Slf4j
public abstract class AbstractClientWebSocketHandler extends TextWebSocketHandler {

    public abstract void sendMessage(final @NotNull ChatMessage message);

    public abstract void disconnect();

}
