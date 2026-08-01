package dev.aetlas.bootdoctor.rules;

import dev.aetlas.bootdoctor.context.PomInfo;
import dev.aetlas.bootdoctor.context.PomInspector;
import dev.aetlas.bootdoctor.context.ProjectContext;
import dev.aetlas.bootdoctor.model.Finding;
import dev.aetlas.bootdoctor.model.Severity;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public final class ActuatorDependencyRule implements Rule {

    private final PomInspector pomInspector = new PomInspector();

    @Override
    public String id() {
        return RuleIds.ACTUATOR_MISSING;
    }

    @Override
    public String description() {
        return "Actuator dependency missing";
    }

    @Override
    public Severity severity() {
        return Severity.MEDIUM;
    }

    @Override
    public List<Finding> evaluate(ProjectContext context) throws IOException {
        PomInfo pomInfo = pomInspector.inspect(context);
        if (pomInfo.hasDependency("org.springframework.boot", "spring-boot-starter-actuator")) {
            return List.of();
        }
        return List.of(Finding.at(id(), severity(), description(), Path.of("pom.xml")));
    }
}
