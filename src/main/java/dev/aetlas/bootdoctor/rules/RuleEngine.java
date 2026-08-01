package dev.aetlas.bootdoctor.rules;

import dev.aetlas.bootdoctor.context.ProjectContext;
import dev.aetlas.bootdoctor.model.Finding;
import dev.aetlas.bootdoctor.model.Report;
import dev.aetlas.bootdoctor.scoring.ScoreCalculator;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public final class RuleEngine {

    private final List<Rule> rules;
    private final ScoreCalculator scoreCalculator;

    public RuleEngine(List<Rule> rules, ScoreCalculator scoreCalculator) {
        this.rules = List.copyOf(Objects.requireNonNull(rules, "rules must not be null"));
        this.scoreCalculator =
                Objects.requireNonNull(scoreCalculator, "scoreCalculator must not be null");
        validateRules(this.rules);
    }

    public static RuleEngine defaultEngine() {
        return new RuleEngine(
                List.of(
                        new SpringBootMavenRule(),
                        new PlainSecretRule(),
                        new CorsWildcardRule(),
                        new ActuatorDependencyRule(),
                        new DockerfileRule(),
                        new DockerComposeRule(),
                        new TestFilesRule(),
                        new ReadmeRule(),
                        new GlobalExceptionHandlerRule(),
                        new ValidationAnnotationRule()),
                new ScoreCalculator());
    }

    public Report analyze(ProjectContext context) throws IOException {
        Objects.requireNonNull(context, "context must not be null");
        List<Finding> findings = new ArrayList<>();

        Rule buildRule =
                rules.stream()
                        .filter(rule -> RuleIds.BUILD_SPRING_BOOT_MAVEN.equals(rule.id()))
                        .findFirst()
                        .orElseThrow(() -> new IllegalStateException("BUILD-001 rule is required"));
        List<Finding> buildFindings = buildRule.evaluate(context);
        if (!buildFindings.isEmpty()) {
            return new Report(context, scoreCalculator.calculate(buildFindings), buildFindings);
        }

        for (Rule rule : rules) {
            if (rule != buildRule) {
                findings.addAll(rule.evaluate(context));
            }
        }
        return new Report(context, scoreCalculator.calculate(findings), findings);
    }

    public List<Rule> rules() {
        return rules;
    }

    private void validateRules(List<Rule> rules) {
        Set<String> ids = new HashSet<>();
        for (Rule rule : rules) {
            Objects.requireNonNull(rule, "rules must not contain null");
            if (!ids.add(rule.id())) {
                throw new IllegalArgumentException("Duplicate rule id: " + rule.id());
            }
        }
    }
}
