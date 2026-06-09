/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.gamejam.server;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import de.chojo.gamejam.configuration.elements.Docker;
import de.chojo.gamejam.configuration.elements.Plugins;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

import static org.slf4j.LoggerFactory.getLogger;

public class DockerService {
    private final Docker dockerConfig;
    private final DockerClientConfig dockerClientConfig;
    private DockerClient dockerClient;
    private static final Logger log = getLogger(DockerService.class);
    private static final String DOCKER_VOLUME_DATA_DIR = "/data";
    private final String pluginUrls;

    public DockerService(Docker dockerConfig, Plugins pluginsConfig) {
        this.dockerConfig = dockerConfig;
        this.pluginUrls = String.join(",", pluginsConfig.defaultPlugins());
        this.dockerClientConfig = DefaultDockerClientConfig
                .createDefaultConfigBuilder()
                .withDockerHost(dockerConfig.getHost())
                .withDockerCertPath(dockerConfig.getCertPath())
                .withDockerTlsVerify(dockerConfig.isTlsVerify())
                .withRegistryUsername(dockerConfig.registryUsername())
                .withRegistryPassword(dockerConfig.registryPassword())
                .withRegistryEmail(dockerConfig.registryEmail())
                .withRegistryUrl(dockerConfig.registryUrl())
                .build();
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
                .withNameFilter(dockerConfig.networkName())
                .exec();
        if (networks.stream().noneMatch(n -> n.getName().equals(dockerConfig.networkName()))) {
            dockerClient.createNetworkCmd()
                    .withName(dockerConfig.networkName())
                    .withDriver("bridge")
                    .exec();
            log.info("Created docker network {}", dockerConfig.networkName());
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

        hostConfig.withNetworkMode(dockerConfig.networkName());

        dockerClient.createContainerCmd(dockerConfig.teamServerImage())
                .withName(containerName(teamId))
                .withEnv("EULA=TRUE", "TYPE=PAPER", "VERSION=26.1.2", String.format("PLUGINS=%s", pluginUrls))
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
        dockerClient.startContainerCmd(containerName(teamId)).exec();
    }

    public void stopServer(int teamId) {
        log.info("Stopping server for team {}", teamId);
        dockerClient.stopContainerCmd(containerName(teamId)).exec();
    }

    public void restartServer(int teamId) {
        dockerClient.restartContainerCmd(containerName(teamId)).exec();
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
                .withCmd("rcon-cli", command)
                .exec()
                .getId();
        try {
            dockerClient.execStartCmd(execId)
                    .exec(new ResultCallback.Adapter<Frame>() {
                        @Override
                        public void onNext(Frame frame) {
                            log.info("rcon-cli response for team {}: {}", teamId, new String(frame.getPayload()));
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

    public void copyArchiveToContainer(int teamId, Path source, Path destination) {
        dockerClient.copyArchiveToContainerCmd(containerName(teamId))
                .withHostResource(source.toString())
                .withRemotePath(destination.toString())
                .exec();
    }

    private String volumeName(int teamId) {
        return "plugin-jam-team-" + teamId;
    }

    public String containerName(int teamId) {
        return "plugin-jam-team-" + teamId;
    }
}
