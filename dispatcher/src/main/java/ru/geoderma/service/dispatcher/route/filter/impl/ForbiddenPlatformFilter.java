package ru.geoderma.service.dispatcher.route.filter.impl;

import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import ru.geoderma.service.dispatcher.common.ClientConnectionMessage;
import ru.geoderma.service.dispatcher.route.filter.FilterResult;
import ru.geoderma.service.dispatcher.route.filter.MessageFilter;

@Component
public class ForbiddenPlatformFilter implements MessageFilter {

    @NotNull
    public FilterResult filter(final @NotNull TextMessage message,
                               final @NotNull WebSocketSession sourceSession,
                               final @NotNull WebSocketSession targetSession,
                               final @NotNull ClientConnectionMessage sourceConnectionInfo,
                               final @NotNull ClientConnectionMessage targetConnectionInfo) {
        if (targetConnectionInfo.getForbiddenPlatforms().contains(sourceConnectionInfo.getPlatformId())) {
            return FilterResult.block(
                    String.format("Platform %s is forbidden for %s",
                            sourceConnectionInfo.getPlatformId(),
                            targetConnectionInfo.getBotId())
            );
        }

        return FilterResult.allow();
    }

    @Override
    public int getOrder() {
        return Integer.MIN_VALUE + 1;
    }

}