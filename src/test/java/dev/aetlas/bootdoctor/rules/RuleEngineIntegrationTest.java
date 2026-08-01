package dev.aetlas.bootdoctor.rules;

import static org.assertj.core.api.Assertions.assertThat;

import dev.aetlas.bootdoctor.context.ProjectContext;
import dev.aetlas.bootdoctor.context.ProjectScanner;
import dev.aetlas.bootdoctor.model.Report;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class RuleEngineIntegrationTest {

    @TempDir Path tempDirectory;

    private final ProjectScanner scanner = new ProjectScanner();
    private final RuleEngine engine = RuleEngine.defaultEngine();

    @Test
    void defaultEngineContainsExactlyTenUniqueRules() {
        assertThat(engine.rules()).hasSize(10);
        assertThat(engine.rules()).extracting(Rule::id).doesNotHaveDuplicates();
    }

    @Test
    void cleanFixtureScoresOneHundredWithoutFindings() throws Exception {
        Report report = analyzeFixture("clean-project");

        assertThat(report.score()).isEqualTo(100);
        assertThat(report.findings()).isEmpty();
    }

    @Test
    void problematicFixtureTriggersAllReadinessRulesExceptBuild() throws Exception {
        Report report = analyzeFixture("problematic-project");

        assertThat(report.score()).isZero();
        assertThat(report.findings())
                .extracting(finding -> finding.ruleId())
                .containsExactlyInAnyOrderElementsOf(
                        Set.of(
                                RuleIds.PLAIN_SECRET,
                                RuleIds.CORS_WILDCARD,
                                RuleIds.ACTUATOR_MISSING,
                                RuleIds.DOCKERFILE_MISSING,
                                RuleIds.COMPOSE_MISSING,
                                RuleIds.TESTS_MISSING,
                                RuleIds.README_MISSING,
                                RuleIds.EXCEPTION_HANDLER_MISSING,
                                RuleIds.VALIDATION_MISSING));
        assertThat(report.findings()).allMatch(finding -> !finding.message().contains("admin123"));
    }

    @Test
    void nonSpringProjectOnlyReportsBuildFinding() throws Exception {
        Files.writeString(
                tempDirectory.resolve("pom.xml"),
                "<project><modelVersion>4.0.0</modelVersion></project>");
        ProjectContext context = scanner.scan(tempDirectory);

        Report report = engine.analyze(context);

        assertThat(report.score()).isZero();
        assertThat(report.findings())
                .extracting(finding -> finding.ruleId())
                .containsExactly(RuleIds.BUILD_SPRING_BOOT_MAVEN);
    }

    private Report analyzeFixture(String name) throws Exception {
        return engine.analyze(scanner.scan(fixture(name)));
    }

    private Path fixture(String name) throws URISyntaxException {
        return Path.of(
                Objects.requireNonNull(
                                getClass().getResource("/fixtures/" + name), "fixture not found")
                        .toURI());
    }
}
