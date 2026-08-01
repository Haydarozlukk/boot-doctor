package dev.aetlas.bootdoctor.rules;

import dev.aetlas.bootdoctor.context.ProjectContext;
import dev.aetlas.bootdoctor.model.Finding;
import dev.aetlas.bootdoctor.model.Severity;
import java.util.List;

public final class ReadmeRule implements Rule {

    @Override
    public String id() {
        return RuleIds.README_MISSING;
    }

    @Override
    public String description() {
        return "README.md missing";
    }

    @Override
    public Severity severity() {
        return Severity.LOW;
    }

    @Override
    public List<Finding> evaluate(ProjectContext context) {
        return RuleSupport.hasRootFile(context, "README.md")
                ? List.of()
                : List.of(Finding.of(id(), severity(), description()));
    }
}
