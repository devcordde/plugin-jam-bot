/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2026 DevCord Team and Contributor
 */

package de.chojo.pluginjam.controller;

import de.chojo.pluginjam.model.FileInfo;
import de.chojo.pluginjam.service.FileService;
import de.chojo.pluginjam.service.TeamService;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.*;
import io.micronaut.http.multipart.CompletedFileUpload;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.rules.SecurityRule;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

@Controller("api/server/files")
@Secured(SecurityRule.IS_AUTHENTICATED)
public class FileController {
    private final FileService fileService;
    private final TeamService teamService;

    public FileController(FileService fileService, TeamService teamService) {
        this.fileService = fileService;
        this.teamService = teamService;
    }

    @Get("/list")
    public HttpResponse<List<FileInfo>> listFiles(Authentication authentication, @QueryValue(defaultValue = "") String path) {
        var team = teamService.getUserTeam(Long.parseLong(authentication.getName()));
        return team.map(value -> HttpResponse.ok(fileService.listFiles(value.id(), path)))
                .orElseGet(HttpResponse::notAllowed);

    }

    @Get(value = "/content", produces = "text/plain")
    public HttpResponse<String> getFileContent(Authentication authentication, @QueryValue String path) {
        var team = teamService.getUserTeam(Long.parseLong(authentication.getName()));
        return team.map(value -> HttpResponse.ok(fileService.getFileContent(value.id(), path)))
                .orElseGet(HttpResponse::notAllowed);

    }

    @Post(value = "/content", consumes = "text/plain")
    public HttpResponse<Void> writeFileContent(Authentication authentication, @QueryValue String path, @Body String content) {
        var team = teamService.getUserTeam(Long.parseLong(authentication.getName()));
        if (team.isEmpty()) {
            return HttpResponse.notAllowed();
        }

        fileService.writeFileContent(team.get().id(), path, content.getBytes());
        return HttpResponse.ok();
    }

    @Post(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA)
    public HttpResponse<Void> uploadFile(Authentication authentication, @QueryValue String path, @Part CompletedFileUpload file) {
        var team = teamService.getUserTeam(Long.parseLong(authentication.getName()));
        if (team.isEmpty()) {
            return HttpResponse.notAllowed();
        }

        try {
            fileService.uploadFile(team.get().id(), path, file.getFilename(), file.getBytes());
        } catch (IOException e) {
            return HttpResponse.serverError();
        }
        return HttpResponse.ok();
    }

    @Post("/create")
    public HttpResponse<Void> createFile(Authentication authentication, @QueryValue String path, @QueryValue(defaultValue = "false") boolean isDirectory) {
        var team = teamService.getUserTeam(Long.parseLong(authentication.getName()));
        if (team.isEmpty()) {
            return HttpResponse.notAllowed();
        }
        fileService.createFile(team.get().id(), path, isDirectory);
        return HttpResponse.ok();
    }

    @Post("/delete")
    public HttpResponse<Void> deleteFile(Authentication authentication, @QueryValue String path) {
        var team = teamService.getUserTeam(Long.parseLong(authentication.getName()));
        if (team.isEmpty()) {
            return HttpResponse.notAllowed();
        }

        fileService.deleteFile(team.get().id(), path);
        return HttpResponse.ok();
    }
}
