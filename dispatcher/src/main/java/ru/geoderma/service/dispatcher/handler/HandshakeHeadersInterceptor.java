package ru.geoderma.service.dispatcher.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import ru.geoderma.service.dispatcher.common.ClientConnectionMessage;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class HandshakeHeadersInterceptor implements HandshakeInterceptor {

    private final ObjectMapper objectMapper;

    @Override
    public boolean beforeHandshake(final @NonNull ServerHttpRequest request,
                                   final @NonNull ServerHttpResponse response,
                                   final @NonNull WebSocketHandler wsHandler,
                                   final @NonNull Map<String, Object> attributes) {
        log.debug("Handshake with: {}", request.getRemoteAddress());
        try {
            String connectionInfoJson = request.getHeaders().getFirst("connection_info");
            ClientConnectionMessage connectionInfo = objectMapper.readValue(
                    connectionInfoJson,
                    ClientConnectionMessage.class
            );

            if (connectionInfo.getBotId() == null
                    || connectionInfo.getChatId() == null
                    || connectionInfo.getPlatformId() == null) {
                log.debug("\"connection_info\" header has unfull data inside");
                throw new IllegalArgumentException();
            }

            attributes.put("connection_info", connectionInfo);

            return true;
        } catch (Exception e) {
            log.debug("Handshake failed with: {}", request.getRemoteAddress());
            response.close();
            return false;
        }
    }

    @Override
    public void afterHandshake(final @NonNull ServerHttpRequest request,
                               final @NonNull ServerHttpResponse response,
                               final @NonNull WebSocketHandler wsHandler,
                               final @Nullable Exception exception) {
        log.debug("Handshake succeed with: {}", request.getRemoteAddress());
    }

}
