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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.slf4j.LoggerFactory.getLogger;

public class TeamServer {
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

    public boolean exists() {
        return serverDir().toFile().exists();
    }

    public boolean setup() throws IOException {
        if (!exists()) return false;
        var serverDir = serverDir();
        Files.createDirectories(serverDir);

        var sourceDir = configuration.serverManagement().template();
        try (var files = Files.walk(Path.of(sourceDir))) {
            for (var source : files.toList()) {
                var path = source.relativize(source);
                Files.copy(source, serverDir.resolve(path));
            }
        }
        return true;
    }

    public boolean purge() throws IOException {
        if (!exists()) return false;
        try (var files = Files.walk(serverDir())) {
            files.sorted(Comparator.reverseOrder())
                 .map(Path::toFile)
                 .forEach(File::delete);
        }
        return true;
    }

    public boolean start() {
        if (exists()) return false;
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
        command.add("-server.jar");
        command.add("--max-players");
        command.add(String.valueOf(server.maxPlayers()));
        command.add("--nogui");
        command.add("--port");
        command.add(String.valueOf(port));
        try {
            start = new ProcessBuilder()
                    .directory(serverDir().toFile())
                    .command(command)
                    .start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    public CompletableFuture<Void> stop(boolean restart) {
        if (start != null) {
            send("stop");
            start = null;
            try {
                return new ProcessBuilder()
                        .directory(serverDir().toFile())
                        .command("while", "screen", "-ls", "|", "grep", "-q", screenName() + ";", "do", "sleep", "1;", "done;")
                        .start()
                        .onExit()
                        .whenComplete(Futures.whenComplete(
                                exit -> serverService.stopped(this, restart),
                                err -> log.error("Could not stop server {}", this))
                        )
                        .thenAccept(null);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return CompletableFuture.completedFuture(null);
    }

    public void send(String command) {
        new ProcessBuilder()
                .directory(serverDir().toFile())
                .command(List.of(
                        "screen",
                        "-S",
                        screenName(),
                        "-p",
                        "0",
                        "-X",
                        "stuff",
                        "\"%s^M\"".formatted(command)
                ));
    }

    private Path serverDir() {
        return Path.of("server", teamName());
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
}
