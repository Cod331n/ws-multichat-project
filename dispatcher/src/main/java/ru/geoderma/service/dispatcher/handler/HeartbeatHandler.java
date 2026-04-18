package ru.geoderma.service.dispatcher.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

@Slf4j
@Component
public class HeartbeatHandler {

    @Value("${heartbeat.timeout}")
    private int heartbeatTimeoutSeconds;

    public void heartbeat(Map<String, WebSocketSession> activeSessions) {
        Iterator<Map.Entry<String, WebSocketSession>> iterator = activeSessions.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<String, WebSocketSession> entry = iterator.next();
            String botId = entry.getKey();
            WebSocketSession session = entry.getValue();

            log.debug("Checking {}", session.getAttributes().get("connection_info"));

            if (!session.isOpen()) {
                log.warn("Session {} is not open - removing from active sessions", botId);
                iterator.remove();
                continue;
            }

            Long lastHeartbeat = (Long) session.getAttributes().get("last_heartbeat");
            if (lastHeartbeat != null) {
                long elapsed = System.currentTimeMillis() - lastHeartbeat;

                if (elapsed > heartbeatTimeoutSeconds * 1000L) {
                    log.warn("No heartbeat from {} for {} seconds - closing session",
                            botId, elapsed / 1000);
                    try {
                        session.close(CloseStatus.SESSION_NOT_RELIABLE);
                    } catch (IOException e) {
                        log.error("Error closing not reliable session", e);
                    }

                    iterator.remove();
                }
            }
        }
    }

}
