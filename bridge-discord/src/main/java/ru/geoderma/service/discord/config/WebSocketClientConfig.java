package ru.geoderma.service.discord.config;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.WebSocketConnectionManager;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import ru.geoderma.service.discord.common.ClientConnectionMessage;
import ru.geoderma.service.discord.handler.ClientWebSocketHandler;
import tools.jackson.databind.ObjectMapper;

import java.util.Set;

@Slf4j
@Getter
@Configuration
public class WebSocketClientConfig {

    @Value("${websocket.server.url}")
    private String serverUrl;

    @Value("${client.platform.id}")
    private String platformId;

    @Value("${client.chat.id}")
    private String chatId;

    @Value("${client.bot.id}")
    private String botId;

    @Value("${client.forbidden.platforms:}")
    private String forbiddenPlatforms;

    @Bean
    public WebSocketClient webSocketClient() {
        return new StandardWebSocketClient();
    }

    @Bean
    public ClientConnectionMessage clientConnectionInfo() {
        Set<String> forbidden = forbiddenPlatforms == null || forbiddenPlatforms.isEmpty()
                ? Set.of()
                : Set.of(forbiddenPlatforms.split(","));

        return ClientConnectionMessage.builder()
                .platformId(platformId)
                .chatId(chatId)
                .botId(botId)
                .forbiddenPlatforms(forbidden)
                .build();
    }

    @Bean
    public WebSocketHttpHeaders webSocketHttpHeaders(ClientConnectionMessage connectionInfo,
                                                     ObjectMapper objectMapper) {
        try {
            WebSocketHttpHeaders headers = new WebSocketHttpHeaders();
            String connectionInfoJson = objectMapper.writeValueAsString(connectionInfo);
            headers.add("connection_info", connectionInfoJson);

            return headers;
        } catch (Exception e) {
            log.error("Failed to create WebSocket HTTP upgrade headers", e);
            return new WebSocketHttpHeaders();
        }
    }

    @Bean
    public WebSocketConnectionManager webSocketConnectionManager(WebSocketClient client,
                                                                 ClientWebSocketHandler handler,
                                                                 WebSocketHttpHeaders headers) {
        WebSocketConnectionManager manager = new WebSocketConnectionManager(client, handler, this.serverUrl);
        manager.setHeaders(headers);
        manager.setAutoStartup(false);

        return manager;
    }

}