package dev.aetlas.bootdoctor.rules;

import dev.aetlas.bootdoctor.context.ProjectContext;
import dev.aetlas.bootdoctor.model.Finding;
import dev.aetlas.bootdoctor.model.Severity;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class CorsWildcardRule implements Rule {

    private static final Pattern EXPLICIT_WILDCARD =
            Pattern.compile(
                    "@(?:[A-Za-z_$][\\w$]*\\.)*CrossOrigin\\s*\\([^)]*[\\\"']\\*[\\\"'][^)]*\\)"
                            + "|(?:allowedOrigins|allowedOriginPatterns|addAllowedOrigin|setAllowedOrigins)"
                            + "\\s*\\([^)]*[\\\"']\\*[\\\"'][^)]*\\)",
                    Pattern.DOTALL);
    private static final Pattern DEFAULT_WILDCARD =
            Pattern.compile("^\\s*@(\\w+\\.)*CrossOrigin\\s*(?:\\(\\s*\\))?\\s*$");

    @Override
    public String id() {
        return RuleIds.CORS_WILDCARD;
    }

    @Override
    public String description() {
        return "CORS wildcard detected";
    }

    @Override
    public Severity severity() {
        return Severity.HIGH;
    }

    @Override
    public List<Finding> evaluate(ProjectContext context) throws IOException {
        List<Finding> findings = new ArrayList<>();
        for (Path file : RuleSupport.mainJavaFiles(context)) {
            String content =
                    RuleSupport.withoutJavaComments(Files.readString(context.resolve(file)));
            Matcher explicitMatcher = EXPLICIT_WILDCARD.matcher(content);
            if (explicitMatcher.find()) {
                findings.add(finding(file, lineNumber(content, explicitMatcher.start())));
                continue;
            }

            List<String> lines = content.lines().toList();
            for (int index = 0; index < lines.size(); index++) {
                if (DEFAULT_WILDCARD.matcher(lines.get(index)).matches()) {
                    findings.add(finding(file, index + 1));
                    break;
                }
            }
        }
        return List.copyOf(findings);
    }

    private Finding finding(Path file, int lineNumber) {
        return Finding.at(id(), severity(), "CORS wildcard detected at line " + lineNumber, file);
    }

    private int lineNumber(String content, int characterIndex) {
        return 1
                + (int)
                        content.substring(0, characterIndex)
                                .chars()
                                .filter(value -> value == '\n')
                                .count();
    }
}
