package dev.aetlas.bootdoctor.rules;

import dev.aetlas.bootdoctor.context.ProjectContext;
import dev.aetlas.bootdoctor.model.Finding;
import dev.aetlas.bootdoctor.model.Severity;
import java.util.List;

public final class DockerfileRule implements Rule {

    @Override
    public String id() {
        return RuleIds.DOCKERFILE_MISSING;
    }

    @Override
    public String description() {
        return "Dockerfile missing";
    }

    @Override
    public Severity severity() {
        return Severity.MEDIUM;
    }

    @Override
    public List<Finding> evaluate(ProjectContext context) {
        return RuleSupport.hasRootFile(context, "Dockerfile")
                ? List.of()
                : List.of(Finding.of(id(), severity(), description()));
    }
}
