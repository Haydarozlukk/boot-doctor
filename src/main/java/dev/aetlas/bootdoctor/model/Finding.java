package dev.aetlas.bootdoctor.model;

import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

public record Finding(
        String ruleId,
        Severity severity,
        String message,
        Optional<Path> location) {

    public Finding {
        ruleId = requireNonBlank(ruleId, "ruleId");
        severity = Objects.requireNonNull(severity, "severity must not be null");
        message = requireNonBlank(message, "message");
        location = Objects.requireNonNull(location, "location must not be null")
                .map(Finding::normalizeRelativeLocation);
    }

    public static Finding of(String ruleId, Severity severity, String message) {
        return new Finding(ruleId, severity, message, Optional.empty());
    }

    public static Finding at(String ruleId, Severity severity, String message, Path location) {
        return new Finding(ruleId, severity, message, Optional.of(location));
    }

    private static String requireNonBlank(String value, String fieldName) {
        Objects.requireNonNull(value, fieldName + " must not be null");
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return trimmed;
    }

    private static Path normalizeRelativeLocation(Path location) {
        Objects.requireNonNull(location, "location must not be null");
        if (location.isAbsolute()) {
            throw new IllegalArgumentException("location must be relative to the project root");
        }

        Path normalized = location.normalize();
        if (normalized.toString().isBlank() || normalized.startsWith("..")) {
            throw new IllegalArgumentException("Invalid project-relative location: " + location);
        }
        return normalized;
    }
}

