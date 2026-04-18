package ru.geoderma.service.telegram;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.socket.client.WebSocketConnectionManager;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import ru.geoderma.service.telegram.app.BridgeTelegramBot;
import ru.geoderma.service.telegram.handler.ClientWebSocketHandler;

@Slf4j
@SpringBootApplication
public class BridgeTelegramApplication {

    static void main(final String... args) {
        log.info("Starting service...");
        ConfigurableApplicationContext context = SpringApplication.run(BridgeTelegramApplication.class, args);
        try {
            WebSocketConnectionManager connectionManager = context.getBean(WebSocketConnectionManager.class);
            BridgeTelegramBot telegramBot = context.getBean(BridgeTelegramBot.class);
            ClientWebSocketHandler handler = context.getBean(ClientWebSocketHandler.class);
            handler.setBot(telegramBot); // решение циклической зависимости

            log.info("Opening WebSocket connection with the server...");
            connectionManager.start();

            log.info("Starting bot session...");
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(telegramBot);
            telegramBot.onStart();

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                if (connectionManager.isRunning()) {
                    log.info("Closing WebSocket connection with the server...");
                    connectionManager.stop();
                }

                log.info("Closing session...");
                handler.disconnect();

                log.info("Shutting down bot...");
                telegramBot.onStop();
            }));
        } catch (Exception e) {
            log.error("Service couldn't be activated: {}", e.getLocalizedMessage());
            throw new RuntimeException(e);
        }
    }

}
