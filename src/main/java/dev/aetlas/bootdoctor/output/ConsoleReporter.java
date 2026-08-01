package dev.aetlas.bootdoctor.output;

import dev.aetlas.bootdoctor.model.Finding;
import dev.aetlas.bootdoctor.model.Report;
import dev.aetlas.bootdoctor.model.Severity;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class ConsoleReporter {

    public void write(Report report, Path displayPath, PrintWriter output) {
        Objects.requireNonNull(report, "report must not be null");
        Objects.requireNonNull(displayPath, "displayPath must not be null");
        Objects.requireNonNull(output, "output must not be null");

        output.println("Boot Doctor");
        output.printf("Target path: %s%n", displayPath);
        output.printf("Score: %d/100%n", report.score());
        output.printf("Status: %s%n", ReadinessStatus.from(report));
        output.printf("Findings: %d%n", report.findings().size());

        if (report.findings().isEmpty()) {
            output.println("No production-readiness issues detected.");
            output.flush();
            return;
        }

        output.println();
        for (Finding finding : report.findings()) {
            output.printf(
                    "[%s] %s - %s%n", finding.severity(), finding.ruleId(), finding.message());
            finding.location()
                    .ifPresent(
                            location -> output.printf("  Location: %s%n", portablePath(location)));
        }
        output.println();
        output.println("Summary: " + severitySummary(report));
        output.flush();
    }

    private String severitySummary(Report report) {
        return Stream.of(Severity.values())
                .map(severity -> severity + "=" + report.findingCount(severity))
                .collect(Collectors.joining(", "));
    }

    private String portablePath(Path path) {
        return path.toString().replace('\\', '/');
    }
}
