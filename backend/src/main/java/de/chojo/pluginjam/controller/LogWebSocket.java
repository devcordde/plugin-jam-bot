package de.chojo.pluginjam.controller;

import de.chojo.pluginjam.database.entity.team.Team;
import de.chojo.pluginjam.service.ServerService;
import de.chojo.pluginjam.service.TeamService;
import io.micronaut.http.annotation.Header;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.rules.SecurityRule;
import io.micronaut.websocket.WebSocketSession;
import io.micronaut.websocket.annotation.OnClose;
import io.micronaut.websocket.annotation.OnOpen;
import io.micronaut.websocket.annotation.ServerWebSocket;
import org.slf4j.Logger;
import reactor.core.Disposable;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static org.slf4j.LoggerFactory.getLogger;

@ServerWebSocket("/api/server/logs/ws")
@Secured(SecurityRule.IS_AUTHENTICATED)
public class LogWebSocket {
    private static final Logger log = getLogger(LogWebSocket.class);
    private final ServerService serverService;
    private final TeamService teamService;
    private final Map<String, Disposable> subscriptions = new ConcurrentHashMap<>();

    public LogWebSocket(ServerService serverService, TeamService teamService) {
        this.serverService = serverService;
        this.teamService = teamService;
    }

    @OnOpen
    public void onOpen(WebSocketSession session, Authentication authentication) {
        log.info("[DEBUG_LOG] WebSocket onOpen triggered for session: {}", session.getId());
        if (authentication == null) {
            log.warn("[DEBUG_LOG] No authentication found for session: {}", session.getId());
            session.close();
            return;
        }
        Optional<Team> team = teamService.getUserTeam(Long.parseLong(authentication.getName()));
        if (team.isEmpty()) {
            log.warn("[DEBUG_LOG] No team found for user: {}", authentication.getName());
            session.close();
            return;
        }

        log.info("Opening log websocket for team {} (user {})", team.get().id(), authentication.getName());

        Disposable subscription = serverService.dockerService().streamLogs(team.get().id(), 100)
                .repeat()
                .retryWhen(reactor.util.retry.Retry.fixedDelay(Long.MAX_VALUE, java.time.Duration.ofSeconds(5)))
                .subscribe(event -> {
                    if (session.isOpen()) {
                        session.sendSync(event.getData());
                    }
                }, throwable -> {
                    log.error("[DEBUG_LOG] Permanent error in log stream for team {}", team.get().id(), throwable);
                    session.close();
                });

        subscriptions.put(session.getId(), subscription);
    }

    @io.micronaut.websocket.annotation.OnMessage
    public void onMessage(String message, WebSocketSession session) {
        // Not used
    }

    @OnClose
    public void onClose(WebSocketSession session) {
        log.info("Closing log websocket for session {}", session.getId());
        Disposable subscription = subscriptions.remove(session.getId());
        if (subscription != null && !subscription.isDisposed()) {
            subscription.dispose();
        }
    }
}
