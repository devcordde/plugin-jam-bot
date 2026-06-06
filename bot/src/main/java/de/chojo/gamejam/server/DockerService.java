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
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import de.chojo.gamejam.configuration.elements.Docker;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

import static org.slf4j.LoggerFactory.getLogger;

public class DockerService {
    private DockerClientConfig dockerClientConfig;
    private DockerClient dockerClient;
    private static final Logger log = getLogger(DockerService.class);
    private static final String DOCKER_IMAGE = "itzg/minecraft-server:latest";
    private static final String DOCKER_VOLUME_DATA_DIR = "/data";

    public DockerService(Docker dockerConfig) {
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

        dockerClient.createContainerCmd(DOCKER_IMAGE)
                .withName(containerName(teamId))
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

    public boolean running(int teamId) {
        return dockerClient.listContainersCmd()
                .withShowAll(true)
                .withNameFilter(List.of(containerName(teamId)))
                .exec()
                .stream()
                .anyMatch(container -> container.getState().equals("running"));
    }

    public void sendCommand(int teamId, String command) {
        dockerClient.execCreateCmd(containerName(teamId))
                .withCmd(String.format("mc rcon-cli %s", command))
                .exec();
    }

    public boolean exists(int teamId) {
        return dockerClient.listContainersCmd()
                .withShowAll(true)
                .exec()
                .stream()
                .anyMatch(container -> container.getId().startsWith(containerName(teamId)));
    }

    public Optional<Container> container(int teamId) {
        return dockerClient.listContainersCmd()
                .withShowAll(true)
                .withNameFilter(List.of(containerName(teamId)))
                .exec()
                .stream()
                .findFirst();
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
