package ru.geoderma.service.telegram.app;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.geoderma.service.telegram.common.ChatMessage;
import ru.geoderma.service.telegram.config.WebSocketClientConfig;
import ru.geoderma.service.telegram.handler.ClientWebSocketHandler;

import java.util.Date;

@Slf4j
@Component
@RequiredArgsConstructor
public class BridgeTelegramBot extends TelegramBot {

    private final WebSocketClientConfig config;

    private final ClientWebSocketHandler handler;

    @Override
    public void onStart() {

    }

    @Override
    public void onStop() {

    }

    @Override
    protected void sendMessage(@NotNull final String formattedMessage) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(config.getChatId());
        sendMessage.setText(formattedMessage);
        sendMessage.enableHtml(false);

        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error("Failed to send message: {}", e.getLocalizedMessage());
        }
        log.debug("Sent message: {}", formattedMessage);
    }

    @Override
    public void onUpdateReceived(final Update update) {
        Message tgMessage = update.getMessage();
        ChatMessage message = ChatMessage.builder()
                .platformId(config.getPlatformId())
                .chatId(config.getChatId())
                .messageId(tgMessage.getMessageId().toString())
                .authorName(tgMessage.getFrom().getUserName())
                .authorId(tgMessage.getFrom().getId().toString())
                .content(tgMessage.getText())
                .timestamp(new Date(tgMessage.getDate()).toInstant())
                .build();

        handler.sendMessage(message);
    }

}
