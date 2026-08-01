package dev.aetlas.bootdoctor.context;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public record PomInfo(
        boolean validMavenProject,
        boolean springBootProject,
        Set<String> dependencies,
        Optional<String> parseError) {

    public PomInfo {
        dependencies =
                Set.copyOf(Objects.requireNonNull(dependencies, "dependencies must not be null"));
        parseError = Objects.requireNonNull(parseError, "parseError must not be null");
    }

    public static PomInfo invalid(String parseError) {
        return new PomInfo(false, false, Set.of(), Optional.of(parseError));
    }

    public boolean hasDependency(String groupId, String artifactId) {
        return dependencies.contains(groupId + ":" + artifactId);
    }
}
