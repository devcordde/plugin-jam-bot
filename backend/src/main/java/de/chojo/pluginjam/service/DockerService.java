/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.pluginjam.service;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.exception.NotFoundException;
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

import java.io.IOException;
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
    private static final String DOCKER_VOLUME_DATA_DIR = "/data";
    private final String pluginUrls;

    public DockerService(DockerConfig dockerConfig, PluginsConfig pluginsConfig) {
        this.dockerConfig = dockerConfig;
        this.pluginUrls = pluginsConfig == null ? "" : String.join(",", pluginsConfig.getDefaultPlugins());
        if (dockerConfig != null) {
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
        } else {
            this.dockerClientConfig = null;
        }
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
            log.info("Created docker network {}", dockerConfig.getNetworkName());
        }
    }

    public void shutdown() throws IOException {
        dockerClient.close();
    }

    public void provisionServer(int teamId) {
        log.info("Provisioning server for team {}", teamId);
        dockerClient.createVolumeCmd()
                .withName(volumeName(teamId))
                .exec();

        HostConfig hostConfig = HostConfig.newHostConfig()
                .withBinds(new Bind(volumeName(teamId), new Volume(DOCKER_VOLUME_DATA_DIR)));

        hostConfig.withNetworkMode(dockerConfig.getNetworkName());

        dockerClient.createContainerCmd(dockerConfig.getTeamServerImage())
                .withName(containerName(teamId))
                .withEnv("EULA=TRUE", "TYPE=PAPER", "VERSION=26.1.2", "CREATE_CONSOLE_IN_PIPE=true", String.format("PLUGINS=%s", pluginUrls))
                .withHostConfig(hostConfig)
                .exec();
        log.info("Server provisioned for team with container name {} and volume name {}", containerName(teamId), volumeName(teamId));
    }

    public void destroyServer(int teamId) {
        log.info("Destroying server for team {}", teamId);
        dockerClient.removeContainerCmd(containerName(teamId)).exec();
        dockerClient.removeVolumeCmd(volumeName(teamId)).exec();
    }

    public void startServer(int teamId) {
        log.info("Starting server for team {}", teamId);
        try {
            ensureNetworkConnection(teamId);
            dockerClient.startContainerCmd(containerName(teamId)).exec();
        } catch (Exception e) {
            log.error("Failed to start server for team {}", teamId, e);
        }
    }

    private void ensureNetworkConnection(int teamId) {
        var container = container(teamId);
        if (container.isEmpty()) return;

        var networkName = dockerConfig.getNetworkName();
        var networkSettings = container.get().getNetworkSettings();
        if (networkSettings != null && networkSettings.getNetworks() != null) {
            if (networkSettings.getNetworks().containsKey(networkName)) {
                // Already connected to the network name, but maybe it's the wrong ID?
                // Docker Java API doesn't easily show the network ID in the list of containers' network settings
                // without inspecting the container.
                try {
                    var inspect = dockerClient.inspectContainerCmd(container.get().getId()).exec();
                    var network = inspect.getNetworkSettings().getNetworks().get(networkName);
                    if (network != null) {
                        var networks = dockerClient.listNetworksCmd().withNameFilter(networkName).exec();
                        var networkExists = networks.stream().anyMatch(n -> n.getId().equals(network.getNetworkID()));
                        if (networkExists) {
                            return; // Network is fine
                        }
                        log.info("Network {} exists but ID mismatch for team {}. Reconnecting.", networkName, teamId);
                    }
                } catch (Exception e) {
                    log.warn("Failed to inspect container {} for network validation", teamId, e);
                }
            }
        }

        log.info("Ensuring network connection for team {} to {}", teamId, networkName);
        try {
            // Try to disconnect first just in case it's partially connected to a dead network
            try {
                dockerClient.disconnectFromNetworkCmd()
                        .withContainerId(container.get().getId())
                        .withNetworkId(networkName)
                        .withForce(true)
                        .exec();
            } catch (Exception ignored) {
            }

            dockerClient.connectToNetworkCmd()
                    .withContainerId(container.get().getId())
                    .withNetworkId(networkName)
                    .exec();
        } catch (Exception e) {
            log.error("Failed to connect container {} to network {}", teamId, networkName, e);
        }
    }

    public void stopServer(int teamId) {
        log.info("Stopping server for team {}", teamId);
        try {
            dockerClient.stopContainerCmd(containerName(teamId)).exec();
        } catch (Exception e) {
            log.error("Failed to stop server for team {}", teamId, e);
        }
    }

    public void restartServer(int teamId) {
        log.info("Restarting server for team {}", teamId);
        try {
            ensureNetworkConnection(teamId);
            dockerClient.restartContainerCmd(containerName(teamId)).exec();
        } catch (Exception e) {
            log.error("Failed to restart server for team {}", teamId, e);
        }
    }

    public boolean isRunning(int teamId) {
        return dockerClient.listContainersCmd()
                .withShowAll(true)
                .withNameFilter(List.of(containerName(teamId)))
                .exec()
                .stream()
                .anyMatch(container -> container.getState().equals("running"));
    }

    public void sendCommand(int teamId, String command) {
        var container = container(teamId);
        if (container.isEmpty()) {
            log.error("Container not found for team {}", teamId);
            return;
        }
        var execId = dockerClient.execCreateCmd(container.get().getId())
                .withAttachStdout(true)
                .withAttachStderr(true)
                .withUser("1000")
                .withCmd("mc-send-to-console", command)
                .exec()
                .getId();
        try {
            dockerClient.execStartCmd(execId)
                    .exec(new ResultCallback.Adapter<Frame>() {
                        @Override
                        public void onNext(Frame frame) {
                            log.info("console response for team {}: {}", teamId, new String(frame.getPayload()));
                        }
                    })
                    .awaitCompletion();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Interrupted while sending command to team {}", teamId, e);
        }
    }
    public boolean exists(int teamId) {
        return dockerClient.listContainersCmd()
                .withShowAll(true)
                .withNameFilter(List.of(containerName(teamId)))
                .exec()
                .stream()
                .findAny()
                .isPresent();
    }

    public Optional<Container> container(int teamId) {
        return dockerClient.listContainersCmd()
                .withShowAll(true)
                .withNameFilter(List.of(containerName(teamId)))
                .exec()
                .stream()
                .findFirst();
    }

    public String logs(int teamId) {
        var callback = new ResultCallback.Adapter<Frame>() {
            private final StringBuilder logs = new StringBuilder();

            @Override
            public void onNext(Frame frame) {
                logs.append(new String(frame.getPayload()));
            }

            public String getLogs() {
                return logs.toString();
            }
        };

        try {
            dockerClient.logContainerCmd(containerName(teamId))
                    .withStdOut(true)
                    .withStdErr(true)
                    .exec(callback)
                    .awaitCompletion();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Interrupted while retrieving logs for team {}", teamId, e);
        }

        return callback.getLogs();
    }

    public Flux<Event<String>> streamLogs(int teamId, int tail) {
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
                    .withTail(tail);

            log.info("Starting log stream for container {}", containerName(teamId));
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

    public void copyArchiveToContainer(int teamId, Path source, Path destination) {
        dockerClient.copyArchiveToContainerCmd(containerName(teamId))
                .withHostResource(source.toString())
                .withRemotePath(destination.toString())
                .exec();
    }

    public static String getContainerHealth(DockerClient dockerClient, String containerId) {
        InspectContainerResponse inspect = dockerClient.inspectContainerCmd(containerId).exec();

        if (inspect.getState() != null && inspect.getState().getHealth() != null) {
            // "starting", "healthy", "unhealthy"
            return inspect.getState().getHealth().getStatus();
        }

        // Falls kein Healthcheck aktiv ist, greift der normale Status (running, exited, etc.)
        return inspect.getState() != null ? inspect.getState().getStatus() : "unknown";
    }

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

    private String volumeName(int teamId) {
        return "plugin-jam-team-" + teamId;
    }

    public String containerName(int teamId) {
        return "plugin-jam-team-" + teamId;
    }
}
