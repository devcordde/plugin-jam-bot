package de.chojo.gamejam.server;

import de.chojo.gamejam.configuration.Configuration;
import de.chojo.gamejam.data.wrapper.team.JamTeam;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class TeamServer {
    private final ServerService serverService;
    private final JamTeam team;
    private final Configuration configuration;
    private final int port;
    private final int apiPort;
    private final List<String> aikar = List.of(
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
            "-Dusing.aikars.flags=https://mcflags.emc.gs -Daikars.new.flags=true"
    );
    private Process start;


    public TeamServer(ServerService serverService, JamTeam team, Configuration configuration, int port, int apiPort) {
        this.serverService = serverService;
        this.team = team;
        this.configuration = configuration;
        this.port = port;
        this.apiPort = apiPort;
    }

    public boolean exists() {
        return serverDir().toFile().exists();
    }

    public void setup() throws IOException {
        var serverDir = serverDir();
        Files.createDirectories(serverDir);

        var sourceDir = configuration.serverManagement().template();
        try (var files = Files.walk(Path.of(sourceDir))) {
            for (var source : files.toList()) {
                var path = source.relativize(source);
                Files.copy(source, serverDir.resolve(path));
            }
        }
    }

    public void purge() throws IOException {
        try (var files = Files.walk(serverDir())) {
            files.sorted(Comparator.reverseOrder())
                 .map(Path::toFile)
                 .forEach(File::delete);
        }
    }

    public void start() {
        var server = configuration.serverManagement();
        var command = new ArrayList<String>();
        command.add("screen");
        command.add("-dmS");
        command.add(screenName());
        command.add("java");
        command.add("-Xms%dM".formatted(server.memory()));
        command.add("-Xms%dM".formatted(server.memory()));
        command.addAll(aikar);
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
    }

    public void stop() {
        if (start != null) start.destroy();
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
        return team.name().toLowerCase().replace(" ", "_");
    }

    private String screenName() {
        return "team_%d_%d".formatted(team.id(), port);
    }
}
