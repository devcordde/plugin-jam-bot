/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2022 DevCord Team and Contributor
 */

package de.chojo.pluginjam.controller;

import de.chojo.pluginjam.database.entity.team.Team;
import de.chojo.pluginjam.service.TeamService;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
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
}
