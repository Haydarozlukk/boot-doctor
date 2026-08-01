package dev.aetlas.bootdoctor.rules;

import dev.aetlas.bootdoctor.context.ProjectContext;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

final class RuleSupport {

    private RuleSupport() {}

    static boolean hasRootFile(ProjectContext context, String... names) {
        return context.files().stream()
                .filter(path -> path.getNameCount() == 1)
                .anyMatch(path -> matchesAny(path.getFileName().toString(), names));
    }

    static List<Path> mainJavaFiles(ProjectContext context) {
        return context.findFiles(
                path -> isJava(path) && containsSegments(path, "src", "main", "java"));
    }

    static List<Path> testFiles(ProjectContext context) {
        return context.findFiles(path -> isJava(path) && containsSegments(path, "src", "test"));
    }

    static List<Path> configurationFiles(ProjectContext context) {
        return context
                .findFiles(
                        path -> {
                            String name = path.getFileName().toString().toLowerCase(Locale.ROOT);
                            return name.startsWith("application-")
                                    || name.equals("application.yml")
                                    || name.equals("application.yaml")
                                    || name.equals("application.properties");
                        })
                .stream()
                .filter(
                        path -> {
                            String name = path.getFileName().toString().toLowerCase(Locale.ROOT);
                            return name.endsWith(".yml")
                                    || name.endsWith(".yaml")
                                    || name.endsWith(".properties");
                        })
                .toList();
    }

    static List<String> readLines(ProjectContext context, Path file) throws IOException {
        return Files.readAllLines(context.resolve(file));
    }

    static String withoutJavaComments(String source) {
        StringBuilder result = new StringBuilder(source.length());
        boolean lineComment = false;
        boolean blockComment = false;
        boolean stringLiteral = false;
        boolean characterLiteral = false;
        boolean escaped = false;

        for (int index = 0; index < source.length(); index++) {
            char current = source.charAt(index);
            char next = index + 1 < source.length() ? source.charAt(index + 1) : '\0';

            if (lineComment) {
                if (current == '\n' || current == '\r') {
                    lineComment = false;
                    result.append(current);
                } else {
                    result.append(' ');
                }
                continue;
            }

            if (blockComment) {
                if (current == '*' && next == '/') {
                    result.append("  ");
                    index++;
                    blockComment = false;
                } else {
                    result.append(current == '\n' || current == '\r' ? current : ' ');
                }
                continue;
            }

            if (!stringLiteral && !characterLiteral && current == '/' && next == '/') {
                result.append("  ");
                index++;
                lineComment = true;
                continue;
            }
            if (!stringLiteral && !characterLiteral && current == '/' && next == '*') {
                result.append("  ");
                index++;
                blockComment = true;
                continue;
            }

            result.append(current);
            if (escaped) {
                escaped = false;
            } else if ((stringLiteral || characterLiteral) && current == '\\') {
                escaped = true;
            } else if (!characterLiteral && current == '"') {
                stringLiteral = !stringLiteral;
            } else if (!stringLiteral && current == '\'') {
                characterLiteral = !characterLiteral;
            }
        }
        return result.toString();
    }

    static boolean containsSegments(Path path, String... segments) {
        if (path.getNameCount() < segments.length) {
            return false;
        }
        for (int start = 0; start <= path.getNameCount() - segments.length; start++) {
            boolean match = true;
            for (int offset = 0; offset < segments.length; offset++) {
                if (!path.getName(start + offset).toString().equals(segments[offset])) {
                    match = false;
                    break;
                }
            }
            if (match) {
                return true;
            }
        }
        return false;
    }

    private static boolean isJava(Path path) {
        return path.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".java");
    }

    private static boolean matchesAny(String actual, String... expected) {
        for (String value : expected) {
            if (actual.equalsIgnoreCase(value)) {
                return true;
            }
        }
        return false;
    }
}
