package ru.geoderma.service.telegram.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
public class ChatMessage {

    private String platformId;

    private String chatId;

    private String messageId;

    private String authorId;

    private String authorName;

    private String content;

    private Instant timestamp;

    private Map<String, Object> metadata;

}
