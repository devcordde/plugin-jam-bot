/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.server;

import de.chojo.gamejam.configuration.Configuration;
import de.chojo.gamejam.data.dao.guild.jams.jam.teams.Team;
import de.chojo.jdautil.util.Futures;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static org.slf4j.LoggerFactory.getLogger;

public class TeamServer {
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
    private Process start;


    public TeamServer(ServerService serverService, Team team, Configuration configuration, int port, int apiPort) {
        this.serverService = serverService;
        this.team = team;
        this.configuration = configuration;
        this.port = port;
        this.apiPort = apiPort;
    }

    public boolean running() {
        return start != null;
    }

    public boolean exists() {
        return serverDir().toFile().exists();
    }

    public boolean setup() throws IOException {
        if (exists()) return false;
        log.info("Setting up server of team {}", team);
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
                    Files.createSymbolicLink(serverTarget, sourceTarget.toAbsolutePath());
                } else {
                    Files.copy(sourceTarget, serverTarget, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
        return true;
    }

    public boolean purge() throws IOException {
        if (!exists()) return false;
        log.info("Purging server of team {}", team);
        try (var files = Files.walk(serverDir())) {
            files.sorted(Comparator.reverseOrder())
                 .map(Path::toFile)
                 .forEach(File::delete);
        }
        return true;
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
        command.add("-Dpluginjam.name=" + teamName());
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
            start = new ProcessBuilder()
                    .directory(serverDir().toFile())
                    .command(command)
                    .redirectOutput(ProcessBuilder.Redirect.to(processLogFile("start")))
                    .start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    public CompletableFuture<Void> stop(boolean restart) {
        if (start != null) {
            send("stop");
            log.info("Stopping server of team {}", team);
            start = null;
            try {
                return new ProcessBuilder()
                        .directory(serverDir().toFile())
                        .command("sh", "while", "screen", "-ls", "|", "grep", "-q", screenName() + ";", "do", "sleep", "1;", "done;")
                        .redirectOutput(ProcessBuilder.Redirect.to(processLogFile("stop")))
                        .start()
                        .onExit()
                        .whenComplete(Futures.whenComplete(
                                exit -> {
                                    log.info("Stopped server of team {}", team);
                                    serverService.stopped(this, restart);
                                },
                                err -> log.error("Could not stop server {}", this))
                        )
                        .thenApply(r -> null);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return CompletableFuture.completedFuture(null);
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
}
