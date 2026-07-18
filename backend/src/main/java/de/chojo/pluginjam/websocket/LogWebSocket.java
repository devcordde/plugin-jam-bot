package de.chojo.pluginjam.websocket;

import de.chojo.pluginjam.database.entity.team.Team;
import de.chojo.pluginjam.service.DockerService;
import de.chojo.pluginjam.service.ServerService;
import de.chojo.pluginjam.service.TeamService;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.rules.SecurityRule;
import io.micronaut.websocket.WebSocketSession;
import io.micronaut.websocket.annotation.OnClose;
import io.micronaut.websocket.annotation.OnMessage;
import io.micronaut.websocket.annotation.OnOpen;
import io.micronaut.websocket.annotation.ServerWebSocket;
import org.slf4j.Logger;
import reactor.core.Disposable;

import java.time.Duration;
import java.util.Optional;

import static org.slf4j.LoggerFactory.getLogger;

@ServerWebSocket("/api/server/logs/ws")
@Secured(SecurityRule.IS_AUTHENTICATED)
public class LogWebSocket {
    private static final Logger log = getLogger(LogWebSocket.class);
    private final DockerService dockerService;
    private final TeamService teamService;

    public LogWebSocket(ServerService serverService, DockerService dockerService, TeamService teamService) {
        this.dockerService = dockerService;
        this.teamService = teamService;
    }

    @OnOpen
    public void onOpen(WebSocketSession session, Authentication authentication) {
        Optional<Team> team = teamService.getUserTeam(Long.parseLong(authentication.getName()));
        if (team.isEmpty()) {
            session.close();
            return;
        }

        log.debug("Starting log stream for session: {}", session.getId());
        Disposable subscription = dockerService.streamLogs(team.get().id())
                .bufferTimeout(10, Duration.ofMillis(100))
                .onBackpressureBuffer()
                .subscribe(events -> {
                    if (session.isOpen()) {
                        var batch = new StringBuilder();
                        for (var event : events) {
                            batch.append(event.getData()).append("\n");
                        }

                        session.sendAsync(batch.toString());
                    }
                }, throwable -> {
                    log.error("Permanent error in log stream for team {}", team.get().id(), throwable);
                    session.close();
                }, () -> {
                    log.debug("Log stream for team {} ended", team.get().id());
                    if (session.isOpen()) session.close();
                });

        session.put("logSubscription", subscription);
    }

    @OnMessage
    public void onMessage(String message, WebSocketSession session) {
        // Not used
    }

    @OnClose
    public void onClose(WebSocketSession session) {
        log.debug("Closing log websocket for session {}", session.getId());
        session.get("logSubscription", Disposable.class).ifPresent(Disposable::dispose);
    }
}
