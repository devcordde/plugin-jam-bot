/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2026 DevCord Team and Contributor
 */

package de.chojo.pluginjam.service;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import de.chojo.pluginjam.bot.config.DockerConfig;
import de.chojo.pluginjam.bot.config.PluginsConfig;
import de.chojo.pluginjam.model.ServerStatus;
import io.micronaut.http.sse.Event;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

import static org.slf4j.LoggerFactory.getLogger;

@Singleton
public class DockerService {
    private final DockerConfig dockerConfig;
    private final DockerClientConfig dockerClientConfig;
    private DockerClient dockerClient;
    private static final Logger log = getLogger(DockerService.class);
    private final String pluginUrls;

    public DockerService(DockerConfig dockerConfig, PluginsConfig pluginsConfig) {
        this.dockerConfig = dockerConfig;
        this.pluginUrls = pluginsConfig == null ? "" : String.join(",", pluginsConfig.getDefaultPlugins());
        this.dockerClientConfig = DefaultDockerClientConfig
                .createDefaultConfigBuilder()
                .withDockerHost(dockerConfig.getHost())
                .withDockerCertPath(dockerConfig.getCertPath())
                .withDockerTlsVerify(dockerConfig.isTlsVerify())
                .withRegistryUsername(dockerConfig.getRegistryUsername())
                .withRegistryPassword(dockerConfig.getRegistryPassword())
                .withRegistryEmail(dockerConfig.getRegistryEmail())
                .withRegistryUrl(dockerConfig.getRegistryUrl())
                .build();

        initDockerClient();
    }

    public void initDockerClient() {
        DockerHttpClient httpClient = new ApacheDockerHttpClient.Builder()
                .dockerHost(dockerClientConfig.getDockerHost())
                .sslConfig(dockerClientConfig.getSSLConfig())
                .maxConnections(100)
                .connectionTimeout(Duration.ofSeconds(30))
                .responseTimeout(Duration.ofSeconds(45))
                .build();

        dockerClient = DockerClientImpl.getInstance(dockerClientConfig, httpClient);
        ensureNetwork();
    }

    private void ensureNetwork() {
        var networks = dockerClient.listNetworksCmd()
                .withNameFilter(dockerConfig.getNetworkName())
                .exec();
        if (networks.stream().noneMatch(n -> n.getName().equals(dockerConfig.getNetworkName()))) {
            dockerClient.createNetworkCmd()
                    .withName(dockerConfig.getNetworkName())
                    .withDriver("bridge")
                    .exec();
            dockerClient.connectToNetworkCmd()
                    .withNetworkId(dockerConfig.getNetworkName())
                    .withContainerId(dockerConfig.getNetworkName())
                    .exec();
            log.info("Created docker network {}", dockerConfig.getNetworkName());
        }
    }

    /**
     * Provisions a server for the given team.
     * 1. Creates a team folder in the data path.
     * 2. Creates a container with the specified image and binds the team folder to the data volume.
     * @param teamId The ID of the team.
     */
    public void provisionServer(int teamId) {
        log.info("Provisioning server for team {}", teamId);
        var teamFolder = teamFolderName(teamId);
        var volumeMount = dockerClient.inspectVolumeCmd(dockerConfig.getVolumeName()).exec().getMountpoint();

        var folder = new File(dockerConfig.getDataPath(), teamFolder);

        if (!folder.exists()) {
            if (!folder.mkdirs()) {
                log.error("Failed to create team folder {}", folder.getAbsolutePath());
            }
        }

        Bind teamBind = new Bind(Path.of(volumeMount).resolve(teamFolder).toAbsolutePath().toString(), new Volume("/data"), AccessMode.rw);

        HostConfig hostConfig = HostConfig.newHostConfig()
                .withBinds(teamBind)
                .withNetworkMode(dockerConfig.getNetworkName());

        dockerClient.createContainerCmd(dockerConfig.getTeamServerImage())
                .withName(containerName(teamId))
                .withEnv("EULA=TRUE", "TYPE=PAPER", "VERSION=26.1.2", "CREATE_CONSOLE_IN_PIPE=true", String.format("PLUGINS=%s", pluginUrls))
                .withHostConfig(hostConfig)
                .exec();
        log.info("Server provisioned for team with container name {}", containerName(teamId));
    }

    /**
     * Destroys the server for the given team.
     * 1. Deletes the team folder.
     * 2. Removes the container.
     *
     * @param teamId The ID of the team.
     */
    public void destroyServer(int teamId) {
        log.info("Destroying server for team {}", teamId);
        var teamFolder = teamFolderName(teamId);

        var folder = new File(dockerConfig.getDataPath(), teamFolder);

        try {
            Files.deleteIfExists(folder.toPath());
        } catch (IOException e) {
            log.error("Failed to delete team folder {}", folder.getAbsolutePath(), e);
        }

        dockerClient.removeContainerCmd(containerName(teamId)).exec();
    }

    /**
     * Starts the server for the given team.
     *
     * @param teamId The ID of the team.
     */
    public void startServer(int teamId) {
        log.info("Starting server for team {}", teamId);
        try {
            dockerClient.startContainerCmd(containerName(teamId)).exec();
        } catch (Exception e) {
            log.error("Failed to start server for team {}", teamId, e);
        }
    }

    /**
     * Stops the server for the given team.
     *
     * @param teamId The ID of the team.
     */
    public void stopServer(int teamId) {
        log.info("Stopping server for team {}", teamId);
        try {
            dockerClient.stopContainerCmd(containerName(teamId)).exec();
        } catch (Exception e) {
            log.error("Failed to stop server for team {}", teamId, e);
        }
    }

    /**
     * Restarts the server for the given team.
     *
     * @param teamId The ID of the team.
     */
    public void restartServer(int teamId) {
        log.info("Restarting server for team {}", teamId);
        try {
            dockerClient.restartContainerCmd(containerName(teamId)).exec();
        } catch (Exception e) {
            log.error("Failed to restart server for team {}", teamId, e);
        }
    }

    /**
     * Sends a command to the mc server console.
     *
     * @param teamId  the id of the team the command should be sent to.
     * @param command the command to send.
     */
    public void sendCommand(int teamId, String command) {
        var container = container(teamId);
        if (container.isEmpty()) {
            log.error("Container not found for team {}", teamId);
            return;
        }
        var execId = dockerClient.execCreateCmd(container.get().getId())
                .withAttachStdout(true)
                .withAttachStderr(true)
                .withUser("1000") // Commands have to be executed as user 1000 see https://docker-minecraft-server.readthedocs.io/en/latest/sending-commands/commands/
                .withCmd("mc-send-to-console", command)
                .exec()
                .getId();

        dockerClient.execStartCmd(execId).exec(new ResultCallback.Adapter<Frame>() {
            @Override
            public void onNext(Frame frame) {
                log.info("console response for team {}: {}", teamId, new String(frame.getPayload()));
            }
        });
    }


    /**
     * Check if a container exists for the given team id.
     *
     * @param teamId the team id of the server.
     * @return true if the container exists, false otherwise
     */
    public boolean exists(int teamId) {
        return dockerClient.listContainersCmd()
                .withShowAll(true)
                .withNameFilter(List.of(containerName(teamId)))
                .exec()
                .stream()
                .findAny()
                .isPresent();
    }

    /**
     * Get a container for the given team id.
     *
     * @param teamId the team id of the server.
     * @return the container, if it exists, empty otherwise
     */
    public Optional<Container> container(int teamId) {
        return dockerClient.listContainersCmd()
                .withShowAll(true)
                .withNameFilter(List.of(containerName(teamId)))
                .exec()
                .stream()
                .findFirst();
    }

    public Flux<Event<String>> streamLogs(int teamId) {
        return Flux.create(sink -> {
            var callback = new ResultCallback.Adapter<Frame>() {
                private String partialLine = "";

                @Override
                public void onNext(Frame frame) {
                    String payload = new String(frame.getPayload());
                    String[] lines = (partialLine + payload).split("\n", -1);
                    for (int i = 0; i < lines.length - 1; i++) {
                        sink.next(Event.of(lines[i]));
                    }
                    partialLine = lines[lines.length - 1];
                }

                @Override
                public void onError(Throwable throwable) {
                    sink.error(throwable);
                }

                @Override
                public void onComplete() {
                    if (!partialLine.isEmpty()) {
                        sink.next(Event.of(partialLine));
                    }
                    sink.complete();
                }
            };

            var cmd = dockerClient.logContainerCmd(containerName(teamId))
                    .withStdOut(true)
                    .withStdErr(true)
                    .withFollowStream(true)
                    .withTail(100);

            var closeable = cmd.exec(callback);

            sink.onCancel(() -> {
                try {
                    closeable.close();
                } catch (IOException e) {
                    log.error("Error closing log stream for team {}", teamId, e);
                }
            });
        }, FluxSink.OverflowStrategy.BUFFER);
    }

    /**
     * Get the status of the server.
     *
     * @param teamId the team id of the server.
     * @return the status of the server.
     */
    public ServerStatus serverStatus(int teamId) {
        try {
            InspectContainerResponse inspect = dockerClient.inspectContainerCmd(containerName(teamId)).exec();
            InspectContainerResponse.ContainerState state = inspect.getState();

            if (state == null) {
                return ServerStatus.VOID;
            }

            if (Boolean.TRUE.equals(state.getRestarting()) ||
                    "paused".equalsIgnoreCase(state.getStatus()) ||
                    "removing".equalsIgnoreCase(state.getStatus())) {
                return ServerStatus.STARTING_STOPPING;
            }

            if (Boolean.TRUE.equals(state.getRunning())) {

                if (state.getHealth() != null) {
                    String healthStatus = state.getHealth().getStatus(); // "starting", "healthy", "unhealthy"

                    switch (healthStatus) {
                        case "starting":
                            return ServerStatus.STARTING_STOPPING;

                        case "healthy":
                            return ServerStatus.RUNNING;

                        case "unhealthy":
                            return ServerStatus.VOID;
                    }
                }

                return ServerStatus.RUNNING;
            }

            return ServerStatus.STOPPED;

        } catch (Exception e) {
            return ServerStatus.VOID;
        }
    }


    /**
     * Get the name of the container for the given team id.
     *
     * @param teamId the team id of the server.
     * @return the name of the container.
     */
    public String containerName(int teamId) {
        return "plugin-jam-team-" + teamId;
    }

    /**
     * Get the name of the team folder.
     *
     * @param teamId the team id
     * @return the name of the team folder.
     */
    public String teamFolderName(int teamId) {
        return "team-" + teamId;
    }
}