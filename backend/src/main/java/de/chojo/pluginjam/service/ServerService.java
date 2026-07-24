/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2026 DevCord Team and Contributor
 */

package de.chojo.pluginjam.service;

import de.chojo.pluginjam.model.payload.PowerSignalPayload;
import jakarta.inject.Singleton;
import org.slf4j.Logger;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import static org.slf4j.LoggerFactory.getLogger;

@Singleton
public class ServerService {
    private static final Logger log = getLogger(ServerService.class);
    private final DockerService dockerService;
    private final Set<Integer> provisioningTeams = ConcurrentHashMap.newKeySet();

    public ServerService(DockerService dockerService) {
        this.dockerService = dockerService;
    }

    public void provisionServer(int teamId) {
        if (provisioningTeams.contains(teamId) || dockerService.exists(teamId)) {
            return;
        }
        provisioningTeams.add(teamId);
        CompletableFuture.runAsync(() -> {
            try {
                dockerService.provisionServer(teamId);
            } finally {
                provisioningTeams.remove(teamId);
            }
        });
    }

    public void reinstallServer(int teamId) {
        log.info("Reinstalling server for team {}", teamId);
        if (provisioningTeams.contains(teamId)) {
            return;
        }
        provisioningTeams.add(teamId);
        CompletableFuture.runAsync(() -> {
            try {
                dockerService.stopServer(teamId);
                dockerService.destroyServer(teamId);
                dockerService.provisionServer(teamId);
            } catch (Exception e) {
                log.error("Failed to reinstall server for team {}", teamId, e);
            } finally {
                provisioningTeams.remove(teamId);
            }
        });
    }

    public boolean isProvisioning(int teamId) {
        return provisioningTeams.contains(teamId);
    }

    public void startServer(int id) {
        dockerService.startServer(id);
        //TODO: register to velocity
    }

    public void stopServer(int id) {
        //TODO: unregister from velocity
        dockerService.stopServer(id);
    }

    public void restartServer(int id) {
        dockerService.restartServer(id);
    }

    public void handlePowerSignal(PowerSignalPayload signal, int teamId) {
        log.info("Handling power signal {} for team {}", signal.signal(), teamId);
        if (signal.signal() == PowerSignalPayload.Signal.START) {
            startServer(teamId);
        } else if (signal.signal() == PowerSignalPayload.Signal.STOP) {
            stopServer(teamId);
        } else if (signal.signal() == PowerSignalPayload.Signal.RESTART) {
            restartServer(teamId);
        } else {
            log.warn("Received unknown power signal: {}", signal.signal());
        }
    }
}
