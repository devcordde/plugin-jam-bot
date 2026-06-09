/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.server;

import com.fasterxml.jackson.core.JsonProcessingException;
import de.chojo.gamejam.configuration.Configuration;
import de.chojo.gamejam.data.dao.guild.jams.jam.teams.Team;
import de.chojo.gamejam.data.dao.guild.jams.jam.user.JamUser;
import de.chojo.gamejam.message.EmbedHelper;
import de.chojo.gamejam.util.Mapper;
import de.chojo.jdautil.localization.util.LocalizedEmbedBuilder;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.pluginjam.payload.RequestsPayload;
import de.chojo.pluginjam.payload.StatsPayload;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static org.slf4j.LoggerFactory.getLogger;

public class TeamServer {
    private static final HttpClient http = HttpClient.newHttpClient();
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH:mm:ss.SSSS");
    private static final Logger log = getLogger(TeamServer.class);
    private static final int DEFAULT_API_PORT = 30000;
    private final DockerService dockerService;
    private final Team team;
    private final Configuration configuration;

    public TeamServer(DockerService dockerService, Team team, Configuration configuration) {
        this.dockerService = dockerService;
        this.team = team;
        this.configuration = configuration;
    }

    public boolean isRunning() {
        return dockerService.isRunning(team.id());
    }

    public boolean exists() {
        return dockerService.exists(team.id());
    }

    /**
     * Sets up the server if it doesn't exist yet.
     *
     * @return true if setup was successful.
     * @throws IOException if data could not be written
     */
    public boolean setup() throws IOException {
        if (exists()) return false;
        dockerService.provisionServer(team.id());
        log.info("Setting up server of team {}", team);
        return true;
    }

    /**
     * Delete all the server data.
     *
     * @return true when server was deleted.
     * @throws IOException
     */
    public boolean purge() throws IOException {
        if (!exists()) return false;
        if (isRunning()) {
            log.info("Stopping server for team {}", team);
            stop();
        }
        log.info("Purging server of team {}", team);
        dockerService.destroyServer(team.id());
        return true;
    }

    public boolean start() {
        if (!exists() || isRunning()) return false;
        log.info("Starting server server of team {}", team);
        dockerService.startServer(team.id());
        return true;
    }

    public CompletableFuture restart() {
        return CompletableFuture.runAsync(() -> {
            dockerService.restartServer(team.id());
            log.info("Starting server of team {}", team);
        });
    }

    public CompletableFuture<Void> stop() {
        return CompletableFuture.runAsync(() -> {
            dockerService.stopServer(team.id());
            log.info("Stopping server of team {}", team);
        });
    }

    public void send(String command) {
        log.info("Sending command \"{}\" to server of team {}.", command, team);
        dockerService.sendCommand(team.id(), command);
    }

    private File processLogFile(String type) {
        var processlog = serverDir().resolve("processlog");
        try {
            Files.createDirectories(processlog);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return processlog
                .resolve("%s_%s.log".formatted(type, FORMATTER.format(LocalDateTime.now())))
                .toFile();
    }

    private Path serverDir() {
        return Path.of(configuration.serverManagement().serverDir(), String.valueOf(team().id()));
    }

    private String teamName() {
        return team.meta().name().toLowerCase().replace(" ", "_");
    }

    public Team team() {
        return team;
    }

    @Override
    public String toString() {
        return "TeamServer{" +
                "team=" + team +
                '}';
    }

    public String logs() {

        return dockerService.logs(team.id());
    }

    public boolean replaceWorld(Path newWorld) {
        log.info("Replacing world");
        dockerService.copyArchiveToContainer(team.id(), newWorld, serverDir());
        return true;
    }

    //TODO: replace with docker version
    public boolean deleteDirectory(Path path) {
        try (var files = Files.walk(path)) {
            files.sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        } catch (NoSuchFileException e) {
            return true;
        } catch (IOException e) {
            log.info("Could not delete directory", e);
            return false;
        }
        return true;
    }

    public Path plugins() {
        var plugins = serverDir().resolve("plugins");
        try {
            Files.createDirectories(plugins);
        } catch (IOException e) {
            //ignore
        }
        return plugins;
    }

    public Path world() {
        var plugins = serverDir().resolve("world");
        try {
            Files.createDirectories(plugins);
        } catch (IOException e) {
            //ignore
        }
        return plugins;
    }

    public String status() {
        var status = statusEmoji();
        var ports = "";
        return "%s %s %s".formatted(status, team, ports);
    }

    public Optional<StatsPayload> stats() {
        var request = requestBuilder("v1/stats")
                .GET()
                .build();
        HttpResponse<String> send = null;
        try {
            send = http().send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            log.error("Could not read stats", e);
            return Optional.empty();
        } catch (InterruptedException e) {
            log.error("Interrupted", e);
            return Optional.empty();
        }
        try {
            return Optional.of(Mapper.MAPPER.readValue(send.body(), StatsPayload.class));
        } catch (JsonProcessingException e) {
            log.error("Could not parse status", e);
            return Optional.empty();
        }
    }

    public HttpRequest.Builder requestBuilder(String path) {
        return HttpRequest.newBuilder(URI.create("http://%s:%d/%s".formatted(containerName(), DEFAULT_API_PORT, path)));
    }

    public HttpRequest.Builder requestBuilder(String path, String query) {
        return HttpRequest.newBuilder(URI.create("http://%s:%d/%s?%s".formatted(containerName(), DEFAULT_API_PORT, path, query)));
    }

    public String containerName() {
        return dockerService.containerName(team.id());
    }

    public HttpClient http() {
        return http;
    }

    public String statusEmoji() {
        if (exists() && isRunning()) return "🟢";
        if (exists()) return "🟡";
        return "🔴";
    }

    public Optional<RequestsPayload> serverRequests() {
        var request = requestBuilder("v1/requests").GET().build();
        HttpResponse<String> send;
        try {
            send = http().send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            log.error("Could not connect to server");
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            log.error("Could not connect to server", e);
            throw new RuntimeException(e);
        }
        try {
            return Optional.of(Mapper.MAPPER.readValue(send.body(), RequestsPayload.class));
        } catch (JsonProcessingException e) {
            log.error("Could not parse response", e);
            throw new RuntimeException(e);
        }
    }

    public CompletableFuture<MessageEmbed> detailStatus(EventContext context) {
        return EmbedHelper.embedDetailedStatus(this, context);
    }
}
