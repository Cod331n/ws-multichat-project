package ru.geoderma.service.dispatcher.route.filter;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import ru.geoderma.service.dispatcher.common.ClientConnectionMessage;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Component
public class MessageFilterChain {

    private final List<MessageFilter> filters;

    public MessageFilterChain(final @NotNull List<MessageFilter> filters) {
        this.filters = filters;
        this.filters.sort(Comparator.comparingInt(MessageFilter::getOrder));
    }

    public boolean filter(final @NotNull TextMessage message,
                          final @NotNull WebSocketSession sourceSession,
                          final @NotNull WebSocketSession targetSession,
                          final @NotNull ClientConnectionMessage sourceConnectionInfo,
                          final @NotNull ClientConnectionMessage targetConnectionInfo) {
        for (MessageFilter filter : filters) {
            try {
                FilterResult result = filter.filter(message, sourceSession, targetSession,
                        sourceConnectionInfo, targetConnectionInfo);

                if (!result.allowed()) {
                    String reason = result.reason() == null
                            ? "no details specified"
                            : result.reason();

                    log.debug("Message blocked by {}: {}",
                            filter.getClass().getSimpleName(),
                            reason);

                    if (FilterResult.FilterAction.BLOCK_AND_CLOSE.equals(result.action())) {
                        closeSession(sourceSession, reason);
                    }

                    return false;
                }

                if (FilterResult.FilterAction.SKIP_REMAINING.equals(result.action())) {
                    log.debug("Filter {} requested to skip remaining filters", filter.getClass().getSimpleName());
                    return true;
                }

            } catch (Exception e) {
                log.error("Filter {} has thrown an exception: {}", filter.getClass().getSimpleName(), e.getLocalizedMessage());
                return false;
            }
        }

        return true;
    }

    private void closeSession(final @NotNull WebSocketSession session, final @NotNull String reason) {
        try {
            session.close(new CloseStatus(CloseStatus.NORMAL.getCode(), reason));
            log.info("Closed session {} by filter: {}", session.getId(), reason);
        } catch (IOException e) {
            log.error("Failed to close session {}", session.getId(), e);
        }
    }

}