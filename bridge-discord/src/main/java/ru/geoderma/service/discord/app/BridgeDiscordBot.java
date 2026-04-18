package ru.geoderma.service.discord.app;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import ru.geoderma.service.discord.common.ChatMessage;
import ru.geoderma.service.discord.config.WebSocketClientConfig;
import ru.geoderma.service.discord.handler.ClientWebSocketHandler;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class BridgeDiscordBot extends ListenerAdapter {

    private final WebSocketClientConfig config;

    private final ClientWebSocketHandler handler;

    private JDA jda;

    private MessageChannel discordChannel;

    public void setJda(JDA jda) {
        this.jda = jda;
        this.discordChannel = jda.getTextChannelById(config.getChatId());

        if (discordChannel == null) {
            log.error("Channel with id: {} not found", config.getChatId());
        }
    }

    public void onStart() {
        log.info("Discord bot started");
    }

    public void onStop() {
        log.info("Discord bot stopped");

        if (jda != null) {
            jda.shutdown();
        }
    }

    protected void sendMessage(@NotNull final String formattedMessage) {
        if (discordChannel == null) {
            log.error("Cannot send message: discord channel is null");
            return;
        }

        discordChannel.sendMessage(formattedMessage).queue(
                _ -> log.debug("Sent message: {}", formattedMessage),
                error -> log.error("Failed to send message: {}", error.getMessage())
        );
    }

    public void sendMessage(@NotNull final ChatMessage message) {
        this.sendMessage(String.format(
                "%s [%s]: %s",
                message.getAuthorName(),
                message.getPlatformId(),
                message.getContent()
        ));
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (!event.isFromGuild() || event.getAuthor().isBot()) {
            return;
        }

        Message discordMessage = event.getMessage();

        ChatMessage chatMessage = ChatMessage.builder()
                .platformId(config.getPlatformId())
                .chatId(config.getChatId())
                .messageId(discordMessage.getId())
                .authorName(discordMessage.getAuthor().getName())
                .authorId(discordMessage.getAuthor().getId())
                .content(discordMessage.getContentDisplay())
                .timestamp(Instant.now())
                .build();

        handler.sendMessage(chatMessage);
        log.debug("Forwarded message from Discord: {}", discordMessage.getContentDisplay());
    }

}