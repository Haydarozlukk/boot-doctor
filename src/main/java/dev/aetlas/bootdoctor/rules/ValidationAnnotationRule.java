package dev.aetlas.bootdoctor.rules;

import dev.aetlas.bootdoctor.context.ProjectContext;
import dev.aetlas.bootdoctor.model.Finding;
import dev.aetlas.bootdoctor.model.Severity;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;

public final class ValidationAnnotationRule implements Rule {

    private static final Pattern VALIDATION_ANNOTATION =
            Pattern.compile(
                    "@(?:[A-Za-z_$][\\w$]*\\.)*(Valid|Validated|NotNull|NotBlank|NotEmpty|Size|Min|Max|Positive|PositiveOrZero|"
                            + "Negative|NegativeOrZero|Email|Pattern|Past|PastOrPresent|Future|FutureOrPresent)\\b");

    @Override
    public String id() {
        return RuleIds.VALIDATION_MISSING;
    }

    @Override
    public String description() {
        return "Validation annotation missing";
    }

    @Override
    public Severity severity() {
        return Severity.MEDIUM;
    }

    @Override
    public List<Finding> evaluate(ProjectContext context) throws IOException {
        for (Path file : RuleSupport.mainJavaFiles(context)) {
            String source =
                    RuleSupport.withoutJavaComments(Files.readString(context.resolve(file)));
            for (String line : source.lines().toList()) {
                String trimmed = line.trim();
                if (!trimmed.startsWith("//")
                        && !trimmed.startsWith("*")
                        && VALIDATION_ANNOTATION.matcher(trimmed).find()) {
                    return List.of();
                }
            }
        }
        return List.of(Finding.of(id(), severity(), description()));
    }
}
