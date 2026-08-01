package dev.aetlas.bootdoctor.cli;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import picocli.CommandLine;

class BootDoctorCommandTest {

    @TempDir Path tempDirectory;

    @Test
    void commandCanBeInstantiated() {
        assertThat(new BootDoctorCommand()).isNotNull();
    }

    @Test
    void cleanProjectReturnsSuccess() throws URISyntaxException {
        Path cleanProject = fixture("clean-project");

        CommandExecution execution = execute(cleanProject.toString());

        assertThat(execution.exitCode()).isZero();
        assertThat(execution.standardOutput())
                .containsSubsequence(
                        "Boot Doctor",
                        "Target path: " + cleanProject,
                        "Score: 100/100",
                        "Status: READY",
                        "Findings: 0");
        assertThat(execution.errorOutput()).isEmpty();
    }

    @Test
    void problematicProjectCanFailForCi() throws URISyntaxException {
        CommandExecution execution =
                execute("--fail-on-findings", fixture("problematic-project").toString());

        assertThat(execution.exitCode()).isEqualTo(CommandLine.ExitCode.SOFTWARE);
        assertThat(execution.standardOutput())
                .contains("Score: 0/100")
                .contains("SEC-001")
                .contains("SEC-002")
                .doesNotContain("admin123");
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
    void regularFileReturnsUsageError() throws Exception {
        Path file = Files.writeString(tempDirectory.resolve("pom.xml"), "<project/>");

        CommandExecution execution = execute(file.toString());

        assertThat(execution.exitCode()).isEqualTo(CommandLine.ExitCode.USAGE);
        assertThat(execution.errorOutput()).contains("Target path is not a directory");
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

    private Path fixture(String name) throws URISyntaxException {
        return Path.of(
                Objects.requireNonNull(
                                getClass().getResource("/fixtures/" + name), "fixture not found")
                        .toURI());
    }

    private CommandExecution execute(String... arguments) {
        ByteArrayOutputStream standardOutput = new ByteArrayOutputStream();
        ByteArrayOutputStream errorOutput = new ByteArrayOutputStream();
        CommandLine commandLine = new CommandLine(new BootDoctorCommand());
        commandLine.setColorScheme(CommandLine.Help.defaultColorScheme(CommandLine.Help.Ansi.OFF));
        commandLine.setOut(new PrintWriter(standardOutput, true, StandardCharsets.UTF_8));
        commandLine.setErr(new PrintWriter(errorOutput, true, StandardCharsets.UTF_8));

        int exitCode = commandLine.execute(arguments);
        return new CommandExecution(
                exitCode,
                standardOutput.toString(StandardCharsets.UTF_8),
                errorOutput.toString(StandardCharsets.UTF_8));
    }

    private record CommandExecution(int exitCode, String standardOutput, String errorOutput) {}
}
