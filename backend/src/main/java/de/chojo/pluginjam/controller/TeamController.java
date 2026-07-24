/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2026 DevCord Team and Contributor
 */

package de.chojo.pluginjam.controller;

import de.chojo.pluginjam.model.payload.TeamMetaUpdatePayload;
import de.chojo.pluginjam.database.entity.team.Team;
import de.chojo.pluginjam.service.TeamService;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Put;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.rules.SecurityRule;

import java.util.Optional;

@Controller("api/teams")
@Secured(SecurityRule.IS_AUTHENTICATED)
public class TeamController {
    private final TeamService teamService;

    public TeamController(TeamService teamService) {
        this.teamService = teamService;
    }

    @Get("/my-team")
    public HttpResponse<Team> getMyTeam(Authentication authentication) {
        Optional<Team> team = teamService.getUserTeam(Long.parseLong(authentication.getName()));
        return team.map(HttpResponse::ok).orElseGet(HttpResponse::notFound);
    }

    @Put("/my-team/meta")
    public HttpResponse<Void> updateMyTeamMeta(Authentication authentication, @Body TeamMetaUpdatePayload update) {
        Optional<Team> team = teamService.getUserTeam(Long.parseLong(authentication.getName()));
        if (team.isEmpty()) {
            return HttpResponse.notFound();
        }
        teamService.updateMeta(team.get(), update.projectDescription(), update.projectUrl());
        return HttpResponse.noContent();
    }
}
