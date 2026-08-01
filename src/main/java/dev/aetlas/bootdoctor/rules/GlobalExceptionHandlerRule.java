package dev.aetlas.bootdoctor.rules;

import dev.aetlas.bootdoctor.context.ProjectContext;
import dev.aetlas.bootdoctor.model.Finding;
import dev.aetlas.bootdoctor.model.Severity;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;

public final class GlobalExceptionHandlerRule implements Rule {

    private static final Pattern ADVICE_ANNOTATION =
            Pattern.compile("@(?:[A-Za-z_$][\\w$]*\\.)*(RestControllerAdvice|ControllerAdvice)\\b");

    @Override
    public String id() {
        return RuleIds.EXCEPTION_HANDLER_MISSING;
    }

    @Override
    public String description() {
        return "Global exception handler missing";
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
                if (!trimmed.startsWith("//") && ADVICE_ANNOTATION.matcher(trimmed).find()) {
                    return List.of();
                }
            }
        }
        return List.of(Finding.of(id(), severity(), description()));
    }
}
