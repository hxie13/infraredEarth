package cn.ac.sitp.infrared.service;

import cn.ac.sitp.infrared.config.StorageProperties;
import org.springframework.stereotype.Component;

import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Stream;

@Component
public class StoredFileResolver {

    private final List<Path> allowedRoots;
    private final Path dataRoot;

    public StoredFileResolver(StorageProperties storageProperties) {
        this.dataRoot = normalizePath(storageProperties.getDataRoot());

        Set<Path> roots = new LinkedHashSet<>();
        if (dataRoot != null) {
            roots.add(dataRoot);
        }
        storageProperties.getAllowedRoots().stream()
                .filter(value -> value != null && !value.isBlank())
                .map(this::normalizePath)
                .filter(path -> path != null)
                .forEach(roots::add);
        this.allowedRoots = List.copyOf(roots);
    }

    public Path resolveExistingFile(String storedPath) throws FileNotFoundException {
        if (storedPath == null || storedPath.isBlank()) {
            throw new FileNotFoundException("Stored file path is blank");
        }

        String normalizedInput = storedPath.replace('\\', '/');
        List<Path> candidates = new ArrayList<>();

        Path rawPath = Path.of(storedPath).normalize();
        if (rawPath.isAbsolute()) {
            candidates.add(rawPath.toAbsolutePath().normalize());
        }

        addRelativeCandidates(candidates, extractRelativePath(normalizedInput));
        addRelativeCandidates(candidates, extractPathFromDataSegment(normalizedInput));

        for (Path candidate : candidates) {
            if (!isUnderAllowedRoot(candidate)) {
                continue;
            }
            if (Files.isRegularFile(candidate)) {
                return candidate;
            }
        }

        Path fileNameCandidate = findByFileName(storedPath);
        if (fileNameCandidate != null) {
            return fileNameCandidate;
        }

        throw new FileNotFoundException("Stored file not found: " + storedPath);
    }

    private boolean isUnderAllowedRoot(Path candidate) {
        for (Path root : allowedRoots) {
            if (candidate.startsWith(root)) {
                return true;
            }
        }
        return false;
    }

    private void addRelativeCandidates(List<Path> candidates, String relativePath) {
        if (relativePath == null || relativePath.isBlank()) {
            return;
        }
        Path relative = Path.of(relativePath).normalize();
        for (Path root : allowedRoots) {
            candidates.add(root.resolve(relative).normalize());
        }
    }

    private String extractRelativePath(String normalizedInput) {
        String relativePath = normalizedInput;
        if (relativePath.startsWith("/data/")) {
            return relativePath.substring("/data/".length());
        }
        if (relativePath.startsWith("data/")) {
            return relativePath.substring("data/".length());
        }
        if (relativePath.startsWith("/")) {
            return relativePath.substring(1);
        }
        if (!Path.of(relativePath).isAbsolute()) {
            return relativePath;
        }
        return null;
    }

    private String extractPathFromDataSegment(String normalizedInput) {
        String lowerValue = normalizedInput.toLowerCase(Locale.ROOT);
        int dataIndex = lowerValue.indexOf("/data/");
        if (dataIndex >= 0) {
            return normalizedInput.substring(dataIndex + "/data/".length());
        }
        return null;
    }

    private Path findByFileName(String storedPath) {
        if (dataRoot == null || !Files.isDirectory(dataRoot)) {
            return null;
        }
        Path fileName = Path.of(storedPath).getFileName();
        if (fileName == null) {
            return null;
        }

        try (Stream<Path> pathStream = Files.walk(dataRoot, 4)) {
            return pathStream
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName() != null)
                    .filter(path -> path.getFileName().toString().equalsIgnoreCase(fileName.toString()))
                    .findFirst()
                    .orElse(null);
        } catch (Exception ignored) {
            return null;
        }
    }

    private Path normalizePath(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Path.of(value).toAbsolutePath().normalize();
    }
}
