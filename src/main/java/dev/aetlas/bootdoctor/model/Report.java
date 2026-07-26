package dev.aetlas.bootdoctor.model;

import dev.aetlas.bootdoctor.context.ProjectContext;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public record Report(ProjectContext context, List<Finding> findings) {

    private static final Comparator<Finding> FINDING_ORDER = Comparator
            .comparingInt((Finding finding) -> finding.severity().priority())
            .thenComparing(Finding::ruleId)
            .thenComparing(Finding::message);

    public Report {
        context = Objects.requireNonNull(context, "context must not be null");
        findings = Objects.requireNonNull(findings, "findings must not be null").stream()
                .map(finding -> Objects.requireNonNull(finding, "findings must not contain null"))
                .sorted(FINDING_ORDER)
                .toList();
    }

    public List<Finding> findings(Severity severity) {
        Objects.requireNonNull(severity, "severity must not be null");
        return findings.stream()
                .filter(finding -> finding.severity() == severity)
                .toList();
    }

    public long findingCount(Severity severity) {
        return findings(severity).size();
    }
}

