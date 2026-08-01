package dev.aetlas.bootdoctor.rules;

import dev.aetlas.bootdoctor.context.ProjectContext;
import dev.aetlas.bootdoctor.model.Finding;
import dev.aetlas.bootdoctor.model.Severity;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public final class PlainSecretRule implements Rule {

    private static final Pattern SENSITIVE_KEY =
            Pattern.compile(
                    "(?i)(?:^|[._-])(password|passwd|pwd|secret|token|api[._-]?key|client[._-]?secret|private[._-]?key)$");
    private static final Pattern ENVIRONMENT_PLACEHOLDER = Pattern.compile("^\\$\\{[^}:]+(?::)?}$");

    @Override
    public String id() {
        return RuleIds.PLAIN_SECRET;
    }

    @Override
    public String description() {
        return "Possible plain secret in config file";
    }

    @Override
    public Severity severity() {
        return Severity.CRITICAL;
    }

    @Override
    public List<Finding> evaluate(ProjectContext context) throws IOException {
        List<Finding> findings = new ArrayList<>();
        for (Path file : RuleSupport.configurationFiles(context)) {
            List<String> lines = RuleSupport.readLines(context, file);
            for (int index = 0; index < lines.size(); index++) {
                SecretEntry entry = parseEntry(lines.get(index));
                if (entry != null
                        && SENSITIVE_KEY.matcher(entry.key()).find()
                        && isPlainValue(entry.value())) {
                    findings.add(
                            Finding.at(
                                    id(),
                                    severity(),
                                    "Possible plain secret for key '"
                                            + entry.key()
                                            + "' at line "
                                            + (index + 1),
                                    file));
                }
            }
        }
        return List.copyOf(findings);
    }

    private SecretEntry parseEntry(String line) {
        String trimmed = line.trim();
        if (trimmed.isEmpty() || trimmed.startsWith("#") || trimmed.startsWith("!")) {
            return null;
        }

        int equals = trimmed.indexOf('=');
        int colon = trimmed.indexOf(':');
        int separator = equals < 0 ? colon : colon < 0 ? equals : Math.min(equals, colon);
        if (separator <= 0) {
            return null;
        }

        String key = unquote(trimmed.substring(0, separator).trim());
        String value = removeInlineComment(trimmed.substring(separator + 1).trim());
        return new SecretEntry(key, unquote(value));
    }

    private boolean isPlainValue(String value) {
        if (value.isBlank()) {
            return false;
        }
        String normalized = value.toLowerCase(Locale.ROOT);
        return !normalized.equals("null")
                && !normalized.equals("~")
                && !ENVIRONMENT_PLACEHOLDER.matcher(value).matches()
                && !normalized.startsWith("enc(")
                && !normalized.startsWith("{cipher}")
                && !normalized.startsWith("vault://")
                && !(value.startsWith("@") && value.endsWith("@"));
    }

    private String removeInlineComment(String value) {
        int comment = value.indexOf(" #");
        return comment < 0 ? value.trim() : value.substring(0, comment).trim();
    }

    private String unquote(String value) {
        if (value.length() >= 2) {
            char first = value.charAt(0);
            char last = value.charAt(value.length() - 1);
            if ((first == '"' && last == '"') || (first == '\'' && last == '\'')) {
                return value.substring(1, value.length() - 1).trim();
            }
        }
        return value;
    }

    private record SecretEntry(String key, String value) {}
}
