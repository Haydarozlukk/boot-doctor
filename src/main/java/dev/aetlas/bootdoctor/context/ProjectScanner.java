package dev.aetlas.bootdoctor.context;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public final class ProjectScanner {

    private static final Set<String> EXCLUDED_DIRECTORIES = Set.of(".git", "target");

    public ProjectContext scan(Path targetPath) throws IOException {
        Objects.requireNonNull(targetPath, "targetPath must not be null");
        Path rootPath = targetPath.toAbsolutePath().normalize();

        if (!Files.exists(rootPath)) {
            throw new IllegalArgumentException("Target path does not exist: " + targetPath);
        }
        if (!Files.isDirectory(rootPath)) {
            throw new IllegalArgumentException("Target path is not a directory: " + targetPath);
        }

        List<Path> files = new ArrayList<>();
        Files.walkFileTree(rootPath, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path directory, BasicFileAttributes attributes) {
                if (!directory.equals(rootPath)
                        && EXCLUDED_DIRECTORIES.contains(directory.getFileName().toString())) {
                    return FileVisitResult.SKIP_SUBTREE;
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) {
                if (attributes.isRegularFile()) {
                    files.add(rootPath.relativize(file));
                }
                return FileVisitResult.CONTINUE;
            }
        });

        return new ProjectContext(rootPath, files);
    }
}

