package dev.aetlas.bootdoctor.cli;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import picocli.CommandLine;

class BootDoctorCommandTest {

    @TempDir
    Path tempDirectory;

    @Test
    void commandCanBeInstantiated() {
        assertThat(new BootDoctorCommand()).isNotNull();
    }

    @Test
    void validDirectoryReturnsSuccess() {
        CommandExecution execution = execute(tempDirectory.toString());

        assertThat(execution.exitCode()).isZero();
        assertThat(execution.standardOutput()).containsSubsequence(
                "Boot Doctor",
                "Target path: " + tempDirectory,
                "Status: CLI initialized successfully");
        assertThat(execution.errorOutput()).isEmpty();
    }

    @Test
    void missingPathReturnsUsageError() {
        CommandExecution execution = execute();

        assertThat(execution.exitCode()).isEqualTo(CommandLine.ExitCode.USAGE);
        assertThat(execution.errorOutput()).contains("Missing required parameter: '<path>'");
    }

    @Test
    void invalidPathReturnsUsageError() {
        Path missingPath = tempDirectory.resolve("missing-project");

        CommandExecution execution = execute(missingPath.toString());

        assertThat(execution.exitCode()).isEqualTo(CommandLine.ExitCode.USAGE);
        assertThat(execution.errorOutput())
                .contains("Error: Target path does not exist: " + missingPath);
        assertThat(execution.standardOutput()).isEmpty();
    }

    @Test
    void helpReturnsSuccess() {
        CommandExecution execution = execute("--help");

        assertThat(execution.exitCode()).isZero();
        assertThat(execution.standardOutput())
                .contains("Usage: boot-doctor")
                .contains("production-readiness")
                .contains("<path>");
        assertThat(execution.errorOutput()).isEmpty();
    }

    private CommandExecution execute(String... arguments) {
        ByteArrayOutputStream standardOutput = new ByteArrayOutputStream();
        ByteArrayOutputStream errorOutput = new ByteArrayOutputStream();
        CommandLine commandLine = new CommandLine(new BootDoctorCommand());
        commandLine.setOut(new PrintWriter(standardOutput, true, StandardCharsets.UTF_8));
        commandLine.setErr(new PrintWriter(errorOutput, true, StandardCharsets.UTF_8));

        int exitCode = commandLine.execute(arguments);
        return new CommandExecution(
                exitCode,
                standardOutput.toString(StandardCharsets.UTF_8),
                errorOutput.toString(StandardCharsets.UTF_8));
    }

    private record CommandExecution(int exitCode, String standardOutput, String errorOutput) {
    }
}

