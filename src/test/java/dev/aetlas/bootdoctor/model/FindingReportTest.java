package dev.aetlas.bootdoctor.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dev.aetlas.bootdoctor.context.ProjectContext;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FindingReportTest {

    @TempDir Path tempDirectory;

    @Test
    void findingSupportsOptionalRelativeLocation() {
        Finding finding =
                Finding.at(
                        "SEC-001",
                        Severity.CRITICAL,
                        "Possible plain secret",
                        Path.of("src/main/resources/application.yml"));

        assertThat(finding.location()).contains(Path.of("src/main/resources/application.yml"));
    }

    @Test
    void findingRejectsBlankAndEscapingValues() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> Finding.of(" ", Severity.HIGH, "message"))
                .withMessageContaining("ruleId");
        assertThatIllegalArgumentException()
                .isThrownBy(
                        () -> Finding.at("SEC-001", Severity.HIGH, "message", Path.of("../secret")))
                .withMessageContaining("location");
    }

    @Test
    void reportSortsFindingsAndDefensivelyCopiesInput() {
        ProjectContext context = new ProjectContext(tempDirectory, List.of());
        Finding low = Finding.of("DOC-001", Severity.LOW, "README missing");
        Finding criticalB = Finding.of("SEC-002", Severity.CRITICAL, "CORS wildcard");
        Finding criticalA = Finding.of("SEC-001", Severity.CRITICAL, "Plain secret");
        List<Finding> findings = new ArrayList<>(List.of(low, criticalB, criticalA));

        Report report = new Report(context, 55, findings);
        findings.clear();

        assertThat(report.score()).isEqualTo(55);
        assertThat(report.findings()).containsExactly(criticalA, criticalB, low);
        assertThat(report.findingCount(Severity.CRITICAL)).isEqualTo(2);
        assertThat(report.findings(Severity.LOW)).containsExactly(low);
        assertThatThrownBy(() -> report.findings().add(low))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void reportRejectsOutOfRangeScore() {
        ProjectContext context = new ProjectContext(tempDirectory, List.of());

        assertThatIllegalArgumentException()
                .isThrownBy(() -> new Report(context, 101, List.of()))
                .withMessageContaining("score");
    }
}
