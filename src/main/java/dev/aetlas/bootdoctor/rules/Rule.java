package dev.aetlas.bootdoctor.rules;

import dev.aetlas.bootdoctor.context.ProjectContext;
import dev.aetlas.bootdoctor.model.Finding;
import dev.aetlas.bootdoctor.model.Severity;
import java.io.IOException;
import java.util.List;

public interface Rule {

    String id();

    String description();

    Severity severity();

    List<Finding> evaluate(ProjectContext context) throws IOException;
}
