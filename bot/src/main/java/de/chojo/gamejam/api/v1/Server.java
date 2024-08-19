package de.chojo.gamejam.api.v1;

import de.chojo.gamejam.data.access.Teams;
import de.chojo.gamejam.data.dao.guild.jams.jam.teams.Team;
import de.chojo.gamejam.server.ServerService;
import de.chojo.gamejam.server.TeamServer;
import io.javalin.http.HttpCode;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

import static io.javalin.apibuilder.ApiBuilder.path;
import static io.javalin.apibuilder.ApiBuilder.post;
import static org.slf4j.LoggerFactory.getLogger;

public class Server {
    private final ServerService serverService;
    private final Teams teams;
    private static final Logger log = getLogger(Server.class);

    public Server(ServerService serverService, Teams teams) {
        this.serverService = serverService;
        this.teams = teams;
    }

    public void routes() {
        path("server", () -> {
            post("plugin/{token}", ctx -> {
                String token = ctx.pathParam("token");
                Optional<Team> team = teams.byToken(token);
                if (team.isEmpty()) {
                    ctx.status(HttpCode.NOT_FOUND);
                    return;
                }

                if (!ctx.contentType().equals("application/octet-stream")) {
                    ctx.status(HttpCode.BAD_REQUEST);
                    ctx.result("Use Content-Type: application/octet-stream");
                    return;
                }

                log.info("Received plugin upload request for {}", team.get());

                TeamServer teamServer = serverService.get(team.get());
                var pluginFile = teamServer.plugins().resolve("plugin.jar");
                try (var in = ctx.bodyAsInputStream()) {
                    log.info("Writing plugin to {}", pluginFile);
                    Files.copy(in, pluginFile, StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    log.warn("Could not write file", e);
                    ctx.status(HttpCode.INTERNAL_SERVER_ERROR);
                    return;
                }

                ctx.status(HttpCode.ACCEPTED);
                String restart = ctx.queryParam("restart");
                if ("true".equals(restart) && teamServer.running()) {
                    teamServer.restart();
                } else if (teamServer.running()) {
                    teamServer.send("say Plugin Updated");
                }
            });
        });
    }
}
