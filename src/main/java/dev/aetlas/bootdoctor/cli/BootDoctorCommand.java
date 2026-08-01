package dev.aetlas.bootdoctor.cli;

import dev.aetlas.bootdoctor.BootDoctorVersion;
import dev.aetlas.bootdoctor.context.ProjectContext;
import dev.aetlas.bootdoctor.context.ProjectScanner;
import dev.aetlas.bootdoctor.model.Report;
import dev.aetlas.bootdoctor.output.ConsoleReporter;
import dev.aetlas.bootdoctor.rules.RuleEngine;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;

@Command(
        name = "boot-doctor",
        description =
                "Spring Boot Maven projeleri için production-readiness kontrollerini çalıştırır.",
        mixinStandardHelpOptions = true,
        version = "Boot Doctor " + BootDoctorVersion.VERSION)
public final class BootDoctorCommand implements Callable<Integer> {

    private final ProjectScanner projectScanner;
    private final RuleEngine ruleEngine;
    private final ConsoleReporter consoleReporter;

    @Parameters(
            index = "0",
            arity = "1",
            paramLabel = "<path>",
            description = "Kontrol edilecek proje dizini.")
    private Path targetPath;

    @Option(names = "--fail-on-findings", description = "Finding varsa işlem exit code 1 döndürür.")
    private boolean failOnFindings;

    @Spec private CommandSpec commandSpec;

    public BootDoctorCommand() {
        this(new ProjectScanner(), RuleEngine.defaultEngine(), new ConsoleReporter());
    }

    BootDoctorCommand(
            ProjectScanner projectScanner, RuleEngine ruleEngine, ConsoleReporter consoleReporter) {
        this.projectScanner = projectScanner;
        this.ruleEngine = ruleEngine;
        this.consoleReporter = consoleReporter;
    }

    @Override
    public Integer call() {
        if (!Files.exists(targetPath)) {
            commandSpec
                    .commandLine()
                    .getErr()
                    .printf("Error: Target path does not exist: %s%n", targetPath);
            return CommandLine.ExitCode.USAGE;
        }

        if (!Files.isDirectory(targetPath)) {
            commandSpec
                    .commandLine()
                    .getErr()
                    .printf("Error: Target path is not a directory: %s%n", targetPath);
            return CommandLine.ExitCode.USAGE;
        }

        try {
            ProjectContext context = projectScanner.scan(targetPath);
            Report report = ruleEngine.analyze(context);
            consoleReporter.write(report, targetPath, commandSpec.commandLine().getOut());
            return failOnFindings && !report.findings().isEmpty()
                    ? CommandLine.ExitCode.SOFTWARE
                    : CommandLine.ExitCode.OK;
        } catch (IOException | SecurityException exception) {
            commandSpec
                    .commandLine()
                    .getErr()
                    .printf("Error: Could not analyze target path: %s%n", exception.getMessage());
            return CommandLine.ExitCode.SOFTWARE;
        }
    }
}
