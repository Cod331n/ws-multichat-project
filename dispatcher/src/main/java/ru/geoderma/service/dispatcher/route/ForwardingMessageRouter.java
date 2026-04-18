package ru.geoderma.service.dispatcher.route;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import ru.geoderma.service.dispatcher.common.ClientConnectionMessage;
import ru.geoderma.service.dispatcher.route.filter.MessageFilterChain;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ForwardingMessageRouter implements MessageRouter {

    private final MessageFilterChain filterChain;

    @Override
    public void route(@NotNull final TextMessage textMessage,
                      @NotNull final WebSocketSession source,
                      @NotNull final Map<String, WebSocketSession> sessions) {
        ClientConnectionMessage sourceConnectionInfo = (ClientConnectionMessage) source.getAttributes().get("connection_info");

        sessions.forEach((_, target) -> {
            ClientConnectionMessage targetConnectionInfo = (ClientConnectionMessage) target.getAttributes().get("connection_info");

            if (filterChain.filter(textMessage, source, target, sourceConnectionInfo, targetConnectionInfo)) {
                try {
                    target.sendMessage(textMessage);
                } catch (IOException e) {
                    log.error("Couldn't send message from {} to {}: {}",
                            sourceConnectionInfo.getPlatformId(),
                            targetConnectionInfo.getPlatformId(),
                            e.getLocalizedMessage());
                }
            }

        });
    }

}
