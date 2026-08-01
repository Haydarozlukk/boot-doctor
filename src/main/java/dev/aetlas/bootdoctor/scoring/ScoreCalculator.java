package dev.aetlas.bootdoctor.scoring;

import dev.aetlas.bootdoctor.model.Finding;
import dev.aetlas.bootdoctor.rules.RuleIds;
import java.util.List;
import java.util.Map;

public final class ScoreCalculator {

    private static final Map<String, Integer> PENALTIES =
            Map.ofEntries(
                    Map.entry(RuleIds.BUILD_SPRING_BOOT_MAVEN, 100),
                    Map.entry(RuleIds.PLAIN_SECRET, 20),
                    Map.entry(RuleIds.CORS_WILDCARD, 15),
                    Map.entry(RuleIds.ACTUATOR_MISSING, 10),
                    Map.entry(RuleIds.DOCKERFILE_MISSING, 10),
                    Map.entry(RuleIds.COMPOSE_MISSING, 5),
                    Map.entry(RuleIds.TESTS_MISSING, 15),
                    Map.entry(RuleIds.README_MISSING, 5),
                    Map.entry(RuleIds.EXCEPTION_HANDLER_MISSING, 10),
                    Map.entry(RuleIds.VALIDATION_MISSING, 10));

    public int calculate(List<Finding> findings) {
        int totalPenalty =
                findings.stream()
                        .map(Finding::ruleId)
                        .distinct()
                        .mapToInt(ruleId -> PENALTIES.getOrDefault(ruleId, 0))
                        .sum();
        return Math.max(0, 100 - totalPenalty);
    }

    public int penaltyFor(String ruleId) {
        return PENALTIES.getOrDefault(ruleId, 0);
    }
}
