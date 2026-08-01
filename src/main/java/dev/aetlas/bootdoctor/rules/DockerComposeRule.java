package dev.aetlas.bootdoctor.rules;

import dev.aetlas.bootdoctor.context.ProjectContext;
import dev.aetlas.bootdoctor.model.Finding;
import dev.aetlas.bootdoctor.model.Severity;
import java.util.List;

public final class DockerComposeRule implements Rule {

    @Override
    public String id() {
        return RuleIds.COMPOSE_MISSING;
    }

    @Override
    public String description() {
        return "Docker Compose missing";
    }

    @Override
    public Severity severity() {
        return Severity.LOW;
    }

    @Override
    public List<Finding> evaluate(ProjectContext context) {
        return RuleSupport.hasRootFile(
                        context,
                        "docker-compose.yml",
                        "docker-compose.yaml",
                        "compose.yml",
                        "compose.yaml")
                ? List.of()
                : List.of(Finding.of(id(), severity(), description()));
    }
}
