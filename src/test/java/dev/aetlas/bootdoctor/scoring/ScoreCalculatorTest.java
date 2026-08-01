package dev.aetlas.bootdoctor.scoring;

import static org.assertj.core.api.Assertions.assertThat;

import dev.aetlas.bootdoctor.model.Finding;
import dev.aetlas.bootdoctor.model.Severity;
import dev.aetlas.bootdoctor.rules.RuleIds;
import java.util.List;
import org.junit.jupiter.api.Test;

class ScoreCalculatorTest {

    private final ScoreCalculator calculator = new ScoreCalculator();

    @Test
    void appliesEachRulePenaltyOnlyOnce() {
        List<Finding> findings =
                List.of(
                        Finding.of(RuleIds.PLAIN_SECRET, Severity.CRITICAL, "first"),
                        Finding.of(RuleIds.PLAIN_SECRET, Severity.CRITICAL, "second"),
                        Finding.of(RuleIds.README_MISSING, Severity.LOW, "README missing"));

        assertThat(calculator.calculate(findings)).isEqualTo(75);
    }

    @Test
    void buildFindingForcesScoreToZero() {
        assertThat(
                        calculator.calculate(
                                List.of(
                                        Finding.of(
                                                RuleIds.BUILD_SPRING_BOOT_MAVEN,
                                                Severity.CRITICAL,
                                                "invalid"))))
                .isZero();
    }
}
