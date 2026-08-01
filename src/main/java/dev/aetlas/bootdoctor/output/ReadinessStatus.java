package dev.aetlas.bootdoctor.output;

import dev.aetlas.bootdoctor.model.Report;
import dev.aetlas.bootdoctor.rules.RuleIds;

public enum ReadinessStatus {
    READY,
    GOOD,
    NEEDS_ATTENTION,
    NOT_READY,
    INVALID_PROJECT;

    public static ReadinessStatus from(Report report) {
        boolean invalidProject =
                report.findings().stream()
                        .anyMatch(
                                finding ->
                                        RuleIds.BUILD_SPRING_BOOT_MAVEN.equals(finding.ruleId()));
        if (invalidProject) {
            return INVALID_PROJECT;
        }
        if (report.score() == 100) {
            return READY;
        }
        if (report.score() >= 80) {
            return GOOD;
        }
        if (report.score() >= 60) {
            return NEEDS_ATTENTION;
        }
        return NOT_READY;
    }
}
