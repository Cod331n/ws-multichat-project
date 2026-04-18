package ru.geoderma.service.telegram.app;

import jakarta.validation.constraints.NotNull;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import ru.geoderma.service.telegram.common.ChatMessage;

import java.util.Objects;

public abstract class TelegramBot extends TelegramLongPollingBot {

    public TelegramBot() {
        super(TelegramBot.getEnvToken());
    }

    public abstract void onStart();

    public abstract void onStop();

    protected abstract void sendMessage(@NotNull final String formattedMessage);

    public void sendMessage(@NotNull final ChatMessage message) {
        this.sendMessage(String.format(
                "%s [%s]: %s",
                message.getAuthorName(),
                message.getPlatformId(),
                message.getContent()
        ));
    }

    @Override
    @NotNull
    public String getBotUsername() {
        Objects.requireNonNull(System.getenv("TELEGRAM_BOT_USERNAME"), "\"TELEGRAM_BOT_USERNAME\" env variable is null");
        return System.getenv("TELEGRAM_BOT_USERNAME");
    }

    @NotNull
    private static String getEnvToken() {
        Objects.requireNonNull(System.getenv("TELEGRAM_BOT_TOKEN"), "\"TELEGRAM_BOT_TOKEN\" env variable is null");
        return System.getenv("TELEGRAM_BOT_TOKEN");
    }

}