/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.server;

import com.fasterxml.jackson.core.JsonProcessingException;
import de.chojo.gamejam.configuration.Configuration;
import de.chojo.gamejam.data.dao.guild.jams.jam.teams.Team;
import de.chojo.gamejam.util.Mapper;
import de.chojo.jdautil.localization.util.LocalizedEmbedBuilder;
import de.chojo.jdautil.util.Futures;
import de.chojo.jdautil.wrapper.EventContext;
import de.chojo.pluginjam.payload.RequestsPayload;
import de.chojo.pluginjam.payload.StatsPayload;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.lingala.zip4j.ZipFile;
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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static org.slf4j.LoggerFactory.getLogger;

public class TeamServer {
    private static final HttpClient http = HttpClient.newHttpClient();
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH:mm:ss.SSSS");
    private static final Logger log = getLogger(TeamServer.class);
    private final ServerService serverService;
    private final Team team;
    private final Configuration configuration;
    private final int port;
    private final int apiPort;
    private static final List<String> AIKAR = List.of(
            "-XX:+ParallelRefProcEnabled",
            "-XX:MaxGCPauseMillis=200",
            "-XX:+UnlockExperimentalVMOptions",
            "-XX:+DisableExplicitGC",
            "-XX:+AlwaysPreTouch",
            "-XX:G1NewSizePercent=30",
            "-XX:G1MaxNewSizePercent=40", "-XX:G1HeapRegionSize=8M",
            "-XX:G1ReservePercent=20",
            "-XX:G1HeapWastePercent=5",
            "-XX:G1MixedGCCountTarget=4",
            "-XX:InitiatingHeapOccupancyPercent=15",
            "-XX:G1MixedGCLiveThresholdPercent=90",
            "-XX:G1RSetUpdatingPauseTimePercent=5",
            "-XX:SurvivorRatio=32",
            "-XX:+PerfDisableSharedMem",
            "-XX:MaxTenuringThreshold=1",
            "-Dusing.aikars.flags=https://mcflags.emc.gs",
            "-Daikars.new.flags=true"
    );
    private boolean running;


    public TeamServer(ServerService serverService, Team team, Configuration configuration, int port, int apiPort) {
        this.serverService = serverService;
        this.team = team;
        this.configuration = configuration;
        this.port = port;
        this.apiPort = apiPort;
    }

    public boolean running() {
        return running;
    }

    public boolean exists() {
        return serverDir().toFile().exists();
    }

    /**
     * Sets up the server if it doesn't exist yet.
     *
     * @return true if setup was successful.
     * @throws IOException if data could not be written
     */
    public boolean setup() throws IOException {
        if (exists()) return false;
        log.info("Setting up server of team {}", team);
        writeTemplate();
        return true;
    }

    /**
     * Refresh the files of the server present in the template. This is basically a new setup without purging the data beforehand.
     * <p>
     * Files with the same name will be overridden
     *
     * @return true when the refresh was successful
     */
    public boolean refresh() {
        log.info("Refreshing template files of server {}", team);
        try {
            writeTemplate();
        } catch (IOException e) {
            log.error("Could not refresh template", e);
            return false;
        }
        return true;
    }

    private void writeTemplate() throws IOException {
        var serverDir = serverDir();
        Files.createDirectories(serverDir);

        var sourceDir = Path.of(configuration.serverTemplate().templateDir());
        var symlinks = configuration.serverTemplate().symLinks()
                                    .stream()
                                    .map(sourceDir::resolve)
                                    .collect(Collectors.toSet());
        try (var files = Files.walk(sourceDir)) {
            for (var sourceTarget : files.toList()) {
                // skip root dir
                if (sourceTarget.getNameCount() == 1) continue;
                var filePath = sourceTarget.subpath(1, sourceTarget.getNameCount());
                var serverTarget = serverDir.resolve(filePath);
                if (symlinks.contains(sourceTarget)) {
                    // Not really required since the current and new symlink are probably equal, but the creation will fail otherwise.
                    if (serverTarget.toFile().isFile() && serverTarget.toFile().delete()) {
                        log.debug("Deleted old version of file {}", serverTarget);
                    }
                    Files.createSymbolicLink(serverTarget, sourceTarget.toAbsolutePath());
                } else {
                    // ignore already existing directories
                    if (sourceTarget.toFile().isDirectory() && serverTarget.toFile().exists()) {
                        continue;
                    }
                    Files.copy(sourceTarget, serverTarget, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
    }

    /**
     * Delete all the server data.
     * @return true when server was deleted.
     * @throws IOException
     */
    public boolean purge() throws IOException {
        if (!exists()) return false;
        log.info("Purging server of team {}", team);
        return deleteDirectory(serverDir());
    }

    public boolean start() {
        if (!exists() || running()) return false;
        var server = configuration.serverManagement();
        var command = new ArrayList<String>();
        command.add("screen");
        command.add("-dmS");
        command.add(screenName());
        command.add("java");
        command.add("-Xmx%dM".formatted(server.memory()));
        command.add("-Xms%dM".formatted(server.memory()));
        command.addAll(AIKAR);
        command.addAll(server.parameter());
        command.add("-Dpluginjam.port=" + server.velocityApi());
        command.add("-Dpluginjam.team.id=" + team.id());
        command.add("-Dpluginjam.team.name=" + teamName());
        command.add("-Djavalin.port=" + apiPort);
        command.add("-Dcom.mojang.eula.agree=true");
        command.add("-jar");
        command.add("server.jar");
        command.add("--max-players");
        command.add(String.valueOf(server.maxPlayers()));
        command.add("--nogui");
        command.add("--port");
        command.add(String.valueOf(port));
        log.info("Starting server server of team {}", team);
        try {
            new ProcessBuilder()
                    .directory(serverDir().toFile())
                    .command(command)
                    .redirectOutput(ProcessBuilder.Redirect.to(processLogFile("start")))
                    .start();
            running = true;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    public CompletableFuture<Void> stop() {
        return stop(false);
    }

    public CompletableFuture<Void> restart() {
        return stop(true);
    }

    public CompletableFuture<Void> stop(boolean restart) {
        if (!running) {
            return CompletableFuture.completedFuture(null);
        }
        running = false;
        try {
            CompletableFuture<Void> future = new ProcessBuilder()
                    .directory(new File("").toPath().toAbsolutePath().toFile())
                    .command("./wait.sh", screenName())
                    .redirectOutput(ProcessBuilder.Redirect.to(processLogFile("stop")))
                    .start()
                    .onExit()
                    .whenComplete(Futures.whenComplete(
                            exit -> {
                                log.info("Stopped server of team {}", team);
                                serverService.stopped(this, restart);
                            },
                            err -> log.error("Could not stop server {}", team))
                    )
                    .thenApply(r -> null);
            send("stop");
            log.info("Stopping server of team {}", team);
            return future;
        } catch (IOException e) {
            log.error("Failed to build process builder", e);
            throw new RuntimeException(e);
        }
    }

    public void send(String command) {
        log.info("Sending command \"{}\" to server of team {}.", command, team);
        try {
            new ProcessBuilder()
                    .directory(serverDir().toFile())
                    .redirectOutput(ProcessBuilder.Redirect.to(processLogFile("send")))
                    .command(List.of(
                            "screen",
                            "-S",
                            screenName(),
                            "-p",
                            "0",
                            "-X",
                            "stuff",
                            "%s^M".formatted(command)
                    ))
                    .start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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

    private String screenName() {
        return "team_%d_%d".formatted(team.id(), port);
    }

    public Team team() {
        return team;
    }

    public int port() {
        return port;
    }

    public int apiPort() {
        return apiPort;
    }

    @Override
    public String toString() {
        return "TeamServer{" +
               "team=" + team +
               ", port=" + port +
               ", apiPort=" + apiPort +
               '}';
    }

    public Path logFile() {
        return serverDir().resolve("logs").resolve("latest.log");
    }

    public boolean replaceWorld(Path newWorld) {
        log.info("Replacing world");
        var worldDir = serverDir().resolve("world");
        var tempWorld = serverDir().resolve("t_world");

        try (var zip = new ZipFile(newWorld.toFile())) {
            log.info("Extracting zip file");
            zip.extractAll(tempWorld.toAbsolutePath().toString());
        } catch (IOException e) {
            log.info("Failed to extract zip file", e);
            return false;
        }

        var copyWorld = tempWorld;
        var dirFiles = List.of(tempWorld.toFile().listFiles());

        var dirOffstet = 3;

        if (dirFiles.size() == 1) {
            log.info("No world data found");
            copyWorld = tempWorld.resolve(dirFiles.get(0).getName());
            dirOffstet++;
        }

        if (!copyWorld.resolve("region").toFile().exists()) {
            log.warn("No region directory.");
            return false;
        }

        log.info("Deleting old world");
        if (!deleteDirectory(worldDir)) {
            return false;
        }

        if (copyWorld.resolve("session.lock").toFile().exists()) {
            log.info("Found session lock. Deleting.");
            copyWorld.resolve("session.lock").toFile().delete();
        }

        log.info("Copy new world data.");
        try (var files = Files.walk(copyWorld)) {
            Files.createDirectories(worldDir);
            for (var sourceTarget : files.toList()) {
                // skip root dir
                if (sourceTarget.getNameCount() == dirOffstet) continue;
                var filePath = sourceTarget.subpath(dirOffstet, sourceTarget.getNameCount());
                var serverTarget = worldDir.resolve(filePath);
                Files.copy(sourceTarget, serverTarget, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            log.error("Could not copy world", e);
            return false;
        }

        log.info("Cleaning up temp world");
        return deleteDirectory(tempWorld);
    }

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

    public CompletableFuture<MessageEmbed> detailStatus(EventContext context) {
        return CompletableFuture.supplyAsync(() -> {

            var builder = new LocalizedEmbedBuilder(context.guildLocalizer())
                    .setTitle("%s #%d | %s".formatted(statusEmoji(), team.id(), team.meta().name()));
            if (!exists()) {
                builder.setDescription("teamserver.message.detailstatus.nonexisting.description");
            } else {
                if (running()) {
                    builder.setDescription("teamserver.message.detailstatus.existing.description")
                           .addField("word.ports", "$word.server$: %d%n$word.api$: %d".formatted(port, apiPort), true);
                    stats().ifPresent(stats -> {
                        var memory = stats.memory();
                        builder.addField("word.memory", "$word.used$ %d%n$word.total$: %d%n$word.max$: %d".formatted(memory.usedMb(), memory.totalMb(), memory.maxMb()), true)
                               .addField("word.tps", "1 $word.min$: %.2f%n5 $word.min$: %.2f%n 15 $word.min$: %.2f%n$word.averageticktime$ %.2f".formatted(
                                       stats.tps()[0], stats.tps()[1], stats.tps()[2], stats.averageTickTime()), true)
                               .addField("word.players", String.valueOf(stats.onlinePlayers()), true)
                               .addField("word.system", "$word.activethreads$: %d".formatted(stats.activeThreads()), true);
                    });
                } else {
                    builder.setDescription("word.serversetup")
                           .addField("word.ports", "word.notrunning", true);
                }
            }

            return builder.build();
        });
    }

    public String status() {
        var status = statusEmoji();
        var ports = "";
        if (exists() && running()) {
            ports = "Server: %s Api: %s".formatted(port, apiPort);
        }
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
        return HttpRequest.newBuilder(URI.create("http://localhost:%d/%s".formatted(apiPort(), path)));
    }

    public HttpRequest.Builder requestBuilder(String path, String query) {
        return HttpRequest.newBuilder(URI.create("http://localhost:%d/%s?%s".formatted(apiPort(), path, query)));
    }

    public HttpClient http() {
        return http;
    }

    public void running(boolean running) {
        this.running = running;
    }

    private String statusEmoji() {
        if (exists() && running()) return "🟢";
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
}
