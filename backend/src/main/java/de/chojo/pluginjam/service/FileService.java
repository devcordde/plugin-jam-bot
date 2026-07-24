/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2026 DevCord Team and Contributor
 */

package de.chojo.pluginjam.service;

import de.chojo.pluginjam.bot.config.DockerConfig;
import de.chojo.pluginjam.bot.config.FileConfig;
import de.chojo.pluginjam.model.FileInfo;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Singleton
public class FileService {
    private final Logger log = LoggerFactory.getLogger(FileService.class);
    private final FileConfig fileConfig;

    private final Path basePath;
    private final UserPrincipal minecraftUser;
    private final GroupPrincipal minecraftGroup;

    public FileService(FileConfig fileConfig, DockerConfig dockerConfig) {
        this.fileConfig = fileConfig;
        this.basePath = Paths.get(dockerConfig.getDataPath()).toAbsolutePath().normalize();

        UserPrincipalLookupService lookupService = FileSystems.getDefault().getUserPrincipalLookupService();
        UserPrincipal user = null;
        GroupPrincipal group = null;
        try {
            user = lookupService.lookupPrincipalByName("minecraft");
            group = lookupService.lookupPrincipalByGroupName("minecraft");
        } catch (IOException e) {
            log.warn("Could not find user or group 'minecraft'. File ownership will not be set.");
        }
        this.minecraftUser = user;
        this.minecraftGroup = group;
    }

    public List<FileInfo> listFiles(int teamId, String path) {
        Path teamBase = basePath.resolve(getTeamPath(teamId)).toAbsolutePath().normalize();
        Path requestedPath = Path.of(path).normalize();
        Path absoluteRequestedPath = teamBase.resolve(requestedPath).toAbsolutePath().normalize();

        if (!absoluteRequestedPath.startsWith(teamBase)) {
            log.warn("Blocked path traversal attempt for team {}: {}", teamId, absoluteRequestedPath);
            return List.of();
        }

        File folder = absoluteRequestedPath.toFile();
        File[] rawFiles = folder.listFiles();
        if (rawFiles == null) {
            return List.of();
        }

        List<FileInfo> files = new ArrayList<>();

        for (File file : rawFiles) {
            String name = file.getName();
            if (name.equals("..") || name.equals(".")) continue;

            boolean isDirectory = file.isDirectory();
            Path relativePath = teamBase.relativize(file.toPath());
            String fullPath = relativePath.toString();

            Optional<FileConfig.FileRule> rule = findRule(fullPath);

            boolean readOnly = false;
            if (rule.isPresent()) {
                boolean show = rule.get().isShow();
                if (!show) continue;
                readOnly = rule.get().isReadOnly();
            }

            long size = file.length();
            Instant lastModified = Instant.ofEpochMilli(file.lastModified());

            files.add(new FileInfo(name, fullPath, isDirectory, readOnly, size, lastModified));
        }

        return files;
    }

    public String getFileContent(int teamId, String path) {
        Path teamBase = basePath.resolve(getTeamPath(teamId)).toAbsolutePath().normalize();
        Path absolutePath = teamBase.resolve(path).toAbsolutePath().normalize();
        if (!absolutePath.startsWith(teamBase)) {
            log.warn("Blocked path traversal attempt for team {}: {}", teamId, absolutePath);
            return "";
        }
        Optional<FileConfig.FileRule> rule = findRule(path);
        if (rule.isPresent() && !rule.get().isShow()) {
            return "";
        }

        if (!Files.exists(absolutePath) || Files.isDirectory(absolutePath)) {
            return "";
        }

        try {
            return Files.readString(absolutePath, StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("Failed to read file {} for team {}", path, teamId, e);
        }

        return "";
    }

    public void writeFileContent(int teamId, String path, byte[] content) {
        Path teamBase = basePath.resolve(getTeamPath(teamId)).toAbsolutePath().normalize();
        Path absolutePath = teamBase.resolve(path).toAbsolutePath().normalize();
        if (!absolutePath.startsWith(teamBase)) {
            log.warn("Blocked path traversal attempt for team {}: {}", teamId, absolutePath);
            return;
        }

        try {
            Files.write(absolutePath, content);
            setSecurePermissions(absolutePath, false);
            log.debug("Uploaded file content {} for team {} to {}", path, teamId, absolutePath);
        } catch (IOException e) {
            log.error("Failed to write file content {} for team {}", path, teamId, e);
        }
    }

    public void uploadFile(int teamId, String path, String name, byte[] content) throws IOException {
        Path teamBase = basePath.resolve(getTeamPath(teamId)).toAbsolutePath().normalize();
        Path absolutePath = teamBase.resolve(path).toAbsolutePath().normalize();
        if (!absolutePath.startsWith(teamBase)) {
            log.warn("Blocked path traversal attempt for team {}: {}", teamId, absolutePath);
            return;
        }

        Optional<FileConfig.FileRule> rule = findRule(path);
        if (rule.isPresent() && (!rule.get().isShow() || rule.get().isReadOnly())) {
            log.warn("Blocked upload attempt for team {}: {}", teamId, path);
            return;
        }

        Path file = absolutePath.resolve(name);
        Path parent = file.getParent();
        if (parent != null && !Files.exists(parent)) {
            createSecureDirectory(parent);
        }

        try {
            Files.write(file, content);
            setSecurePermissions(file, false);
            log.debug("Uploaded file {} for team {} to {}", name, teamId, file);
        } catch (IOException e) {
            log.error("Failed to write file {} for team {}", name, teamId, e);
        }
    }

    public void deleteFile(int teamId, String path) {
        Path teamBase = basePath.resolve(getTeamPath(teamId)).toAbsolutePath().normalize();
        Path absolutePath = teamBase.resolve(path).toAbsolutePath().normalize();
        if (!absolutePath.startsWith(teamBase)) {
            log.warn("Blocked path traversal attempt for team {}: {}", teamId, absolutePath);
            return;
        }

        Optional<FileConfig.FileRule> rule = findRule(path);
        if (rule.isPresent() && (!rule.get().isShow() || rule.get().isReadOnly())) {
            log.warn("Blocked delete attempt for team {}: {}", teamId, path);
            return;
        }

        try {
            Files.deleteIfExists(absolutePath);
        } catch (IOException e) {
            log.error("Failed to delete file {} for team {}", path, teamId, e);
        }
    }

    public void createFile(int teamId, String path, boolean isDirectory) {
        Path teamBase = basePath.resolve(getTeamPath(teamId)).toAbsolutePath().normalize();
        Path absolutePath = teamBase.resolve(path).toAbsolutePath().normalize();
        if (!absolutePath.startsWith(teamBase)) {
            log.warn("Blocked path traversal attempt for team {}: {}", teamId, absolutePath);
            return;
        }

        Optional<FileConfig.FileRule> rule = findRule(path);
        if (rule.isPresent() && (!rule.get().isShow() || rule.get().isReadOnly())) {
            log.warn("Blocked create attempt for team {}: {}", teamId, path);
            return;
        }

        if (Files.exists(absolutePath)) {
            return;
        }
        try {
            createSecureDirectory(absolutePath.getParent());
            if (isDirectory) {
                createSecureDirectory(absolutePath);
            } else {
                Files.createFile(absolutePath);
                setSecurePermissions(absolutePath, false);
            }
        } catch (IOException e) {
            log.error("Failed to create file {} for team {}", path, teamId, e);
        }
    }

    private Optional<FileConfig.FileRule> findRule(String path) {
        log.info("Finding rule for path: {}", path);
        for (FileConfig.FileRule rule : fileConfig.getRules()) {
            PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + rule.getPattern());
            Path pathObj = Paths.get(path);
            boolean matches = matcher.matches(pathObj);
            if (matches) {
                return Optional.of(rule);
            }
        }
        log.info("No rule matched for path: {}", path);
        return Optional.empty();
    }

    public void createSecureDirectory(Path dirPath) throws IOException {
        if (Files.exists(dirPath)) return;
        var perms = PosixFilePermissions.fromString("rwxrwxr-x");
        var attr = PosixFilePermissions.asFileAttribute(perms);

        Files.createDirectories(dirPath, attr);
        setSecurePermissions(dirPath, true);
    }

    private void setSecurePermissions(Path path, boolean isDirectory) {
        try {
            var perms = PosixFilePermissions.fromString(isDirectory ? "rwxrwxr-x" : "rw-rw-r--");
            Files.setPosixFilePermissions(path, perms);

            if (minecraftUser != null || minecraftGroup != null) {
                PosixFileAttributeView view = Files.getFileAttributeView(path, PosixFileAttributeView.class);
                if (view != null) {
                    if (minecraftUser != null) {
                        view.setOwner(minecraftUser);
                    }
                    if (minecraftGroup != null) {
                        view.setGroup(minecraftGroup);
                    }
                }
            }
        } catch (IOException e) {
            log.error("Failed to set permissions/owner for {}", path, e);
        }
    }

    private Path getTeamPath(int teamId) {
        return Path.of("team-" + teamId);
    }
}
