package ru.geoderma.service.discord;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.socket.client.WebSocketConnectionManager;
import ru.geoderma.service.discord.app.BridgeDiscordBot;
import ru.geoderma.service.discord.handler.ClientWebSocketHandler;

import java.util.concurrent.CompletableFuture;

@Slf4j
@SpringBootApplication
public class BridgeDiscordApplication {

    static void main(final String... args) {
        log.info("Starting Discord service...");
        ConfigurableApplicationContext context = SpringApplication.run(BridgeDiscordApplication.class, args);

        try {
            WebSocketConnectionManager connectionManager = context.getBean(WebSocketConnectionManager.class);
            BridgeDiscordBot discordBot = context.getBean(BridgeDiscordBot.class);
            ClientWebSocketHandler handler = context.getBean(ClientWebSocketHandler.class);
            handler.setBot(discordBot); // решение циклической зависимости

            log.info("Opening WebSocket connection with the server...");
            connectionManager.start();

            log.info("Starting Discord bot...");
            String token = System.getenv("DISCORD_BOT_TOKEN");
            if (token == null || token.isEmpty()) {
                throw new IllegalStateException("\"DISCORD_BOT_TOKEN\" env variable not set");
            }

            JDA jda = JDABuilder.createDefault(token)
                    .enableIntents(GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT)
                    .addEventListeners(discordBot)
                    .build();

            // 0.о
            CompletableFuture.runAsync(() -> {
                try {
                    jda.awaitReady();
                    log.info("Discord bot is ready");
                } catch (InterruptedException e) {
                    throw new RuntimeException("Failed to initialize JDA for Discord bot");
                }
            }).thenRun(() -> {
                discordBot.setJda(jda);
                discordBot.onStart();
            });

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                if (connectionManager.isRunning()) {
                    log.info("Closing WebSocket connection with the server...");
                    connectionManager.stop();
                }

                log.info("Closing session...");
                handler.disconnect();

                log.info("Shutting down bot...");
                discordBot.onStop();
            }));
        } catch (Exception e) {
            log.error("Service couldn't be activated: {}", e.getLocalizedMessage(), e);
            throw new RuntimeException(e);
        }
    }
}