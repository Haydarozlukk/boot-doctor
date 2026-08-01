package dev.aetlas.bootdoctor.rules;

import dev.aetlas.bootdoctor.context.PomInfo;
import dev.aetlas.bootdoctor.context.PomInspector;
import dev.aetlas.bootdoctor.context.ProjectContext;
import dev.aetlas.bootdoctor.model.Finding;
import dev.aetlas.bootdoctor.model.Severity;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public final class SpringBootMavenRule implements Rule {

    private final PomInspector pomInspector;

    public SpringBootMavenRule() {
        this(new PomInspector());
    }

    SpringBootMavenRule(PomInspector pomInspector) {
        this.pomInspector = pomInspector;
    }

    @Override
    public String id() {
        return RuleIds.BUILD_SPRING_BOOT_MAVEN;
    }

    @Override
    public String description() {
        return "Not a Spring Boot Maven Project";
    }

    @Override
    public Severity severity() {
        return Severity.CRITICAL;
    }

    @Override
    public List<Finding> evaluate(ProjectContext context) throws IOException {
        PomInfo pomInfo = pomInspector.inspect(context);
        if (pomInfo.validMavenProject() && pomInfo.springBootProject()) {
            return List.of();
        }

        String message =
                pomInfo.parseError()
                        .orElse(
                                "pom.xml does not declare a Spring Boot parent, dependency, or Maven plugin");
        if (context.hasFile("pom.xml")) {
            return List.of(Finding.at(id(), severity(), message, Path.of("pom.xml")));
        }
        return List.of(Finding.of(id(), severity(), message));
    }
}
