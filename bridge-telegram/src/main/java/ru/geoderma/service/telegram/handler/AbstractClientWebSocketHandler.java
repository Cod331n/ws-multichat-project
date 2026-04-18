package ru.geoderma.service.telegram.handler;


import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import ru.geoderma.service.telegram.common.ChatMessage;

@Slf4j
public abstract class AbstractClientWebSocketHandler extends TextWebSocketHandler {

    public abstract void sendMessage(final @NotNull ChatMessage message);

    public abstract void disconnect();

}
