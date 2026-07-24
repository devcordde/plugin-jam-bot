/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2026 DevCord Team and Contributor
 */

package de.chojo.pluginjam.controller;

import de.chojo.pluginjam.model.payload.CommandPayload;
import de.chojo.pluginjam.model.payload.PowerSignalPayload;
import de.chojo.pluginjam.model.ServerStatus;
import de.chojo.pluginjam.service.DockerService;import de.chojo.pluginjam.service.ServerService;
import de.chojo.pluginjam.service.TeamService;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.rules.SecurityRule;

import java.util.Map;

@Controller("api/server")
@Secured(SecurityRule.IS_AUTHENTICATED)
public class ServerController {
    private final ServerService serverService;
    private final TeamService teamService;private final DockerService dockerService;

    public ServerController(ServerService serverService, TeamService teamService, DockerService dockerService) {
        this.serverService = serverService;
        this.teamService = teamService;this.dockerService = dockerService;}

    @Get("/status/")
    public HttpResponse<ServerStatus> getStatus(Authentication authentication) {
        var team = teamService.getUserTeam(Long.parseLong(authentication.getName()));
        return team.map(value -> HttpResponse.ok(dockerService.serverStatus(value.id())))
                .orElseGet(HttpResponse::notAllowed);

    }

    @Get("/exists")
    public HttpResponse<Map<String, Object>> exists(Authentication authentication) {
        var team = teamService.getUserTeam(Long.parseLong(authentication.getName()));
        if (team.isEmpty()) {
            return HttpResponse.notAllowed();
        }

        boolean exists = dockerService.exists(team.get().id());
        boolean provisioning = serverService.isProvisioning(team.get().id());
        
        return HttpResponse.ok(Map.of(
                "exists", exists,
                "provisioning", provisioning
        ));
    }

    @Post("/provision")
    public HttpResponse<Void> provision(Authentication authentication) {
        var team = teamService.getUserTeam(Long.parseLong(authentication.getName()));
        if (team.isEmpty()) {
            return HttpResponse.notAllowed();
        }
        serverService.provisionServer(team.get().id());
        return HttpResponse.accepted();
    }

    @Post("/reinstall")
    public HttpResponse<Void> reinstall(Authentication authentication) {
        var team = teamService.getUserTeam(Long.parseLong(authentication.getName()));
        if (team.isEmpty()) {
            return HttpResponse.notAllowed();
        }
        serverService.reinstallServer(team.get().id());
        return HttpResponse.accepted();
    }

    @Post("/power")
    public HttpResponse<Void> powerSignal(Authentication authentication, @Body PowerSignalPayload powerSignalPayload) {
        var team = teamService.getUserTeam(Long.parseLong(authentication.getName()));
        if (team.isEmpty()) {
            return HttpResponse.notAllowed();
        }
        System.out.println("[DEBUG_LOG] Received power signal: " + powerSignalPayload.signal() + " for team: " + team.get().id());
        serverService.handlePowerSignal(powerSignalPayload, team.get().id());
        return HttpResponse.ok();
    }

    @Post("/command")
    public HttpResponse<Void> sendCommand(Authentication authentication, @Body CommandPayload commandPayload) {
        var team = teamService.getUserTeam(Long.parseLong(authentication.getName()));
        if (team.isEmpty()) {
            return HttpResponse.notAllowed();
        }

        dockerService.sendCommand(team.get().id(), commandPayload.command());
        return HttpResponse.ok();
    }
}
