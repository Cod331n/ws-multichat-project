package ru.geoderma.service.dispatcher.route.filter;

import org.jetbrains.annotations.NotNull;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import ru.geoderma.service.dispatcher.common.ClientConnectionMessage;

@FunctionalInterface
public interface MessageFilter {

    @NotNull
    FilterResult filter(final @NotNull TextMessage message,
                        final @NotNull WebSocketSession sourceSession,
                        final @NotNull WebSocketSession targetSession,
                        final @NotNull ClientConnectionMessage sourceConnectionInfo,
                        final @NotNull ClientConnectionMessage targetConnectionInfo);

    /** Чем меньше - тем приоритетнее */
    default int getOrder() {
        return 0;
    }

}
