package dev.aetlas.bootdoctor.cli;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;

@Command(
        name = "boot-doctor",
        description = "Spring Boot projeleri için production-readiness kontrollerini çalıştırır.",
        mixinStandardHelpOptions = true,
        version = "Boot Doctor 0.1.0-SNAPSHOT")
public final class BootDoctorCommand implements Callable<Integer> {

    @Parameters(
            index = "0",
            arity = "1",
            paramLabel = "<path>",
            description = "Kontrol edilecek proje dizini.")
    private Path targetPath;

    @Spec
    private CommandSpec commandSpec;

    @Override
    public Integer call() {
        if (!Files.exists(targetPath)) {
            commandSpec.commandLine().getErr()
                    .printf("Error: Target path does not exist: %s%n", targetPath);
            return CommandLine.ExitCode.USAGE;
        }

        if (!Files.isDirectory(targetPath)) {
            commandSpec.commandLine().getErr()
                    .printf("Error: Target path is not a directory: %s%n", targetPath);
            return CommandLine.ExitCode.USAGE;
        }

        commandSpec.commandLine().getOut().println("Boot Doctor");
        commandSpec.commandLine().getOut().printf("Target path: %s%n", targetPath);
        commandSpec.commandLine().getOut().println("Status: CLI initialized successfully");
        return CommandLine.ExitCode.OK;
    }
}

