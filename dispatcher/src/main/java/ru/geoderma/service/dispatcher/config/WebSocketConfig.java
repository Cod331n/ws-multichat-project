package ru.geoderma.service.dispatcher.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import ru.geoderma.service.dispatcher.handler.HandshakeHeadersInterceptor;
import ru.geoderma.service.dispatcher.handler.ServerWebSocketHandler;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final ServerWebSocketHandler handler;

    private final HandshakeHeadersInterceptor handshakeHeadersInterceptor;

    @Override
    public void registerWebSocketHandlers(final WebSocketHandlerRegistry registry) {
        registry.addHandler(handler, "/ws")
                .addInterceptors(handshakeHeadersInterceptor)
                .setAllowedOrigins("*");
    }

}
