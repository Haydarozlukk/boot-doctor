package dev.aetlas.bootdoctor.context;

import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public record ProjectContext(Path rootPath, List<Path> files) {

    public ProjectContext {
        rootPath =
                Objects.requireNonNull(rootPath, "rootPath must not be null")
                        .toAbsolutePath()
                        .normalize();
        files =
                Objects.requireNonNull(files, "files must not be null").stream()
                        .map(ProjectContext::normalizeRelativePath)
                        .distinct()
                        .sorted(Comparator.comparing(ProjectContext::portablePath))
                        .toList();
    }

    public boolean hasFile(String relativePath) {
        Objects.requireNonNull(relativePath, "relativePath must not be null");
        return hasFile(Path.of(relativePath));
    }

    public boolean hasFile(Path relativePath) {
        return files.contains(normalizeRelativePath(relativePath));
    }

    public List<Path> findFiles(Predicate<Path> predicate) {
        Objects.requireNonNull(predicate, "predicate must not be null");
        return files.stream().filter(predicate).toList();
    }

    public Path resolve(Path relativePath) {
        Path resolved = rootPath.resolve(normalizeRelativePath(relativePath)).normalize();
        if (!resolved.startsWith(rootPath)) {
            throw new IllegalArgumentException("Path must stay inside the project root");
        }
        return resolved;
    }

    private static Path normalizeRelativePath(Path path) {
        Objects.requireNonNull(path, "file path must not be null");
        if (path.isAbsolute()) {
            throw new IllegalArgumentException(
                    "File paths must be relative to the project root: " + path);
        }

        Path normalized = path.normalize();
        if (normalized.toString().isBlank() || normalized.startsWith("..")) {
            throw new IllegalArgumentException("Invalid project-relative path: " + path);
        }
        return normalized;
    }

    private static String portablePath(Path path) {
        return path.toString().replace('\\', '/');
    }
}
