package ru.geoderma.service.dispatcher.route.filter.impl;

import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import ru.geoderma.service.dispatcher.common.ClientConnectionMessage;
import ru.geoderma.service.dispatcher.route.filter.FilterResult;
import ru.geoderma.service.dispatcher.route.filter.MessageFilter;

@Component
public class EchoFilter implements MessageFilter {

    @Override
    public @NotNull FilterResult filter(final @NotNull TextMessage message,
                                        final @NotNull WebSocketSession sourceSession,
                                        final @NotNull WebSocketSession targetSession,
                                        final @NotNull ClientConnectionMessage sourceConnectionInfo,
                                        final @NotNull ClientConnectionMessage targetConnectionInfo) {
        if (sourceConnectionInfo.getBotId().equals(targetConnectionInfo.getBotId())) {
            return FilterResult.block(null);
        }

        return FilterResult.allow();
    }

    @Override
    public int getOrder() {
        return Integer.MIN_VALUE;
    }

}
