package de.chojo.pluginjam.controller;

import de.chojo.pluginjam.model.PowerSignalDTO;
import de.chojo.pluginjam.model.ServerStatus;
import de.chojo.pluginjam.service.ServerService;
import de.chojo.pluginjam.service.TeamService;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.rules.SecurityRule;

@Controller("api/server")
@Secured(SecurityRule.IS_AUTHENTICATED)
public class ServerController {
    private final ServerService serverService;
    private final TeamService teamService;

    public ServerController(ServerService serverService, TeamService teamService) {
        this.serverService = serverService;
        this.teamService = teamService;
    }

    @Get("/status/")
    public HttpResponse<ServerStatus> getStatus(Authentication authentication) {
        var team = teamService.getUserTeam(Long.parseLong(authentication.getName()));
        if (team.isEmpty()) {
            return HttpResponse.notAllowed();
        }

        return HttpResponse.ok(serverService.dockerService().serverStatus(team.get().id()));
    }

    @Get("/exists")
    public HttpResponse<java.util.Map<String, Object>> exists(Authentication authentication) {
        var team = teamService.getUserTeam(Long.parseLong(authentication.getName()));
        if (team.isEmpty()) {
            return HttpResponse.notAllowed();
        }

        boolean exists = serverService.dockerService().exists(team.get().id());
        boolean provisioning = serverService.isProvisioning(team.get().id());
        
        return HttpResponse.ok(java.util.Map.of(
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
    public HttpResponse<Void> powerSignal(Authentication authentication, @Body PowerSignalDTO powerSignal) {
        var team = teamService.getUserTeam(Long.parseLong(authentication.getName()));
        if (team.isEmpty()) {
            return HttpResponse.notAllowed();
        }
        System.out.println("[DEBUG_LOG] Received power signal: " + powerSignal.signal() + " for team: " + team.get().id());
        serverService.handlePowerSignal(powerSignal, team.get().id());
        return HttpResponse.ok();
    }

    @Post("/command")
    public HttpResponse<Void> sendCommand(Authentication authentication, @Body java.util.Map<String, String> body) {
        var team = teamService.getUserTeam(Long.parseLong(authentication.getName()));
        if (team.isEmpty()) {
            return HttpResponse.notAllowed();
        }
        String command = body.get("command");
        if (command == null || command.isBlank()) {
            return HttpResponse.badRequest();
        }
        serverService.dockerService().sendCommand(team.get().id(), command);
        return HttpResponse.ok();
    }

}
