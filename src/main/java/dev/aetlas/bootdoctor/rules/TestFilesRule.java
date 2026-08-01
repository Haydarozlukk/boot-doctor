package dev.aetlas.bootdoctor.rules;

import dev.aetlas.bootdoctor.context.ProjectContext;
import dev.aetlas.bootdoctor.model.Finding;
import dev.aetlas.bootdoctor.model.Severity;
import java.util.List;

public final class TestFilesRule implements Rule {

    @Override
    public String id() {
        return RuleIds.TESTS_MISSING;
    }

    @Override
    public String description() {
        return "No test files found";
    }

    @Override
    public Severity severity() {
        return Severity.HIGH;
    }

    @Override
    public List<Finding> evaluate(ProjectContext context) {
        return RuleSupport.testFiles(context).isEmpty()
                ? List.of(Finding.of(id(), severity(), description()))
                : List.of();
    }
}
