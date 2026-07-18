package de.chojo.pluginjam.service;

import de.chojo.pluginjam.bot.config.FileConfig;
import de.chojo.pluginjam.model.FileInfo;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.FileSystems;
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
    private final DockerService dockerService;
    private final FileConfig fileConfig;

    private static final Path BASE_PATH = Paths.get("/data").toAbsolutePath().normalize();

    public FileService(DockerService dockerService, FileConfig fileConfig) {
        this.dockerService = dockerService;
        this.fileConfig = fileConfig;
    }

    public List<FileInfo> listFiles(int teamId, String relativePath) {
        if (!isValidPath(relativePath)) {
            log.warn("Blocked path traversal attempt for team {}: {}", teamId, relativePath);
            return List.of();
        }

        List<String> rawFiles = dockerService.listFiles(teamId, relativePath);
        List<FileInfo> files = new ArrayList<>();

        for (String raw : rawFiles) {
            String trimmed = raw.trim();
            if (trimmed.isBlank() || trimmed.startsWith("total")) continue;
            // Expected format: drwxr-xr-x 2 minecraft minecraft 4096 1626384000 name/
            String[] parts = trimmed.split("\\s+", 7);
            if (parts.length < 7) continue;

            long size = 0;
            try {
                size = Long.parseLong(parts[4]);
            } catch (NumberFormatException ignored) {
            }

            Instant lastModified = Instant.EPOCH;
            try {
                lastModified = Instant.ofEpochSecond(Long.parseLong(parts[5]));
            } catch (NumberFormatException ignored) {
            }

            String name = parts[6];
            boolean isDirectory = name.endsWith("/");
            if (isDirectory) {
                name = name.substring(0, name.length() - 1);
            }

            if (name.equals("..") || name.equals(".")) continue;

            log.info("Resolved name: '{}', isDirectory: {}", name, isDirectory);
            String fullPath = relativePath.isEmpty() ? name : relativePath + "/" + name;
            log.info("Resolved full path: '{}' (isDirectory: {})", fullPath, isDirectory);

            Optional<FileConfig.FileRule> rule = findRule(fullPath);

            boolean readOnly = false;

            if (rule.isPresent()) {
                boolean show = rule.get().isShow();
                if (!show) continue;
                readOnly = rule.get().isReadOnly();
            }

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

        return dockerService.getFileContent(teamId, path);
    }

    public void uploadFile(int teamId, String path, byte[] content) throws IOException {
        if (!isValidPath(path)) {
            log.warn("Blocked path traversal attempt for team {}: {}", teamId, path);
            return;
        }

        Optional<FileConfig.FileRule> rule = findRule(path);
        if (rule.isPresent() && (rule.get().isShow() || rule.get().isReadOnly())) {
            log.warn("Blocked upload attempt for team {}: {}", teamId, path);
            return;
        }

        dockerService.writeFileContent(teamId, path, content);
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
            Path resolvedPath = BASE_PATH.resolve(relativePath).normalize();
            log.debug("Resolved path: {}", resolvedPath);
            log.debug("Resolved path starts with BASE_PATH: {}", resolvedPath.startsWith(BASE_PATH));
            return resolvedPath.startsWith(BASE_PATH);
        } catch (Exception e) {
            return false;
        }
    }

    public void deleteFile(Integer id, String path) {
        if (!isValidPath(path)) {
            log.warn("Blocked path traversal attempt for team {}: {}", id, path);
            return;
        }

        Optional<FileConfig.FileRule> rule = findRule(path);
        if (rule.isPresent() && (rule.get().isShow() || rule.get().isReadOnly())) {
            log.warn("Blocked delete attempt for team {}: {}", id, path);
            return;
        }

        dockerService.deleteFile(id, path);
    }
}
