package de.chojo.pluginjam.service;

import de.chojo.pluginjam.bot.config.DockerConfig;
import de.chojo.pluginjam.bot.config.FileConfig;
import de.chojo.pluginjam.model.FileInfo;
import io.micronaut.core.io.ResourceResolver;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Singleton
public class FileService {
    private final Logger log = LoggerFactory.getLogger(FileService.class);
    private final FileConfig fileConfig;

    private final Path basePath;

    public FileService(FileConfig fileConfig, DockerConfig dockerConfig) {
        this.fileConfig = fileConfig;
        this.basePath = Paths.get(dockerConfig.getDataPath()).toAbsolutePath().normalize();
    }

    public List<FileInfo> listFiles(int teamId, String relativePath) {
        if (!isValidPath(relativePath)) {
            log.warn("Blocked path traversal attempt for team {}: {}", teamId, relativePath);
            return List.of();
        }

        File folder = getTeamPath(teamId).resolve(relativePath).toFile();
        File[] rawFiles = folder.listFiles();
        if (rawFiles == null) {
            return List.of();
        }

        List<FileInfo> files = new ArrayList<>();

        for (File file : rawFiles) {
            String name = file.getName();
            if (name.equals("..") || name.equals(".")) continue;

            boolean isDirectory = file.isDirectory();
            String fullPath = relativePath.isEmpty() ? name : relativePath + "/" + name;
            log.info("Resolved full path: '{}' (isDirectory: {})", fullPath, isDirectory);

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
        if (!isValidPath(path)) {
            return "";
        }
        Optional<FileConfig.FileRule> rule = findRule(path);
        if (rule.isPresent() && !rule.get().isShow()) {
            return "";
        }

        Path file = getTeamPath(teamId).resolve(path);
        if (!Files.exists(file) || Files.isDirectory(file)) {
            return "";
        }

        try {
            return Files.readString(file, StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("Failed to read file {} for team {}", path, teamId, e);
        }

        return "";
    }

    public void writeFileContent(int teamId, String path, byte[] content) {
        if (!isValidPath(path)) {
            log.warn("Blocked path traversal attempt for team {}: {}", teamId, path);
            return;
        }

        Path file = getTeamPath(teamId).resolve(path);
        try {
            Files.write(file, content);
            log.debug("Uploaded file content {} for team {} to {}", path, teamId, file);
        } catch (IOException e) {
            log.error("Failed to write file content {} for team {}", path, teamId, e);
        }
    }

    public void uploadFile(int teamId, String path, String name, byte[] content) throws IOException {
        if (!isValidPath(path)) {
            log.warn("Blocked path traversal attempt for team {}: {}", teamId, path);
            return;
        }

        Optional<FileConfig.FileRule> rule = findRule(path);
        if (rule.isPresent() && (!rule.get().isShow() || rule.get().isReadOnly())) {
            log.warn("Blocked upload attempt for team {}: {}", teamId, path);
            return;
        }

        Path file = getTeamPath(teamId).resolve(path).resolve(name);
        Path parent = file.getParent();
        if (parent != null && !Files.exists(parent)) {
            Files.createDirectories(parent);
        }

        try {
            Files.write(file, content);
            log.debug("Uploaded file {} for team {} to {}", name, teamId, file);
        } catch (IOException e) {
            log.error("Failed to write file {} for team {}", name, teamId, e);
        }
    }

    public void deleteFile(Integer id, String path) {
        if (!isValidPath(path)) {
            log.warn("Blocked path traversal attempt for team {}: {}", id, path);
            return;
        }

        Optional<FileConfig.FileRule> rule = findRule(path);
        if (rule.isPresent() && (!rule.get().isShow() || rule.get().isReadOnly())) {
            log.warn("Blocked delete attempt for team {}: {}", id, path);
            return;
        }

        Path file = getTeamPath(id).resolve(path);
        try {
            Files.deleteIfExists(file);
        } catch (IOException e) {
            log.error("Failed to delete file {} for team {}", path, id, e);
        }
    }

    public void createFile(Integer id, String path, boolean isDirectory) {
        if (!isValidPath(path)) {
            log.warn("Blocked path traversal attempt for team {}: {}", id, path);
            return;
        }

        Optional<FileConfig.FileRule> rule = findRule(path);
        if (rule.isPresent() && (!rule.get().isShow() || rule.get().isReadOnly())) {
            log.warn("Blocked create attempt for team {}: {}", id, path);
        }

        Path file = getTeamPath(id).resolve(path);
        if (Files.exists(file)) {
            return;
        }
        try {
            Files.createDirectories(file.getParent());
            if (isDirectory) {
                Files.createDirectory(file);
            } else {
                Files.createFile(file);
            }
        } catch (IOException e) {
            log.error("Failed to create file {} for team {}", path, id, e);
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

    private boolean isValidPath(String relativePath) {
        if (relativePath.contains("..") || relativePath.startsWith("/") || relativePath.contains("\\")) {
            return false;
        }
        try {
            Path resolvedPath = basePath.resolve(relativePath).normalize();
            return resolvedPath.startsWith(basePath);
        } catch (Exception e) {
            return false;
        }
    }

    private Path getTeamPath(int teamId) {
        return basePath.resolve("team-" + teamId);
    }

}
