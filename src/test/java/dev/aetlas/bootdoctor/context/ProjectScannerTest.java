package dev.aetlas.bootdoctor.context;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ProjectScannerTest {

    @TempDir Path tempDirectory;

    private final ProjectScanner scanner = new ProjectScanner();

    @Test
    void scansAProjectIntoSortedRelativeFileInventory() throws IOException {
        Files.writeString(tempDirectory.resolve("README.md"), "Boot Doctor");
        Path sourceFile = tempDirectory.resolve("src/main/java/App.java");
        Files.createDirectories(sourceFile.getParent());
        Files.writeString(sourceFile, "class App {}");

        ProjectContext context = scanner.scan(tempDirectory);

        assertThat(context.rootPath()).isEqualTo(tempDirectory.toAbsolutePath().normalize());
        assertThat(context.files())
                .containsExactly(Path.of("README.md"), Path.of("src/main/java/App.java"));
        assertThat(context.hasFile("README.md")).isTrue();
        assertThat(context.resolve(Path.of("README.md")))
                .isEqualTo(tempDirectory.resolve("README.md").toAbsolutePath().normalize());
    }

    @Test
    void excludesGitAndTargetTrees() throws IOException {
        Path gitFile = tempDirectory.resolve(".git/config");
        Path targetFile = tempDirectory.resolve("target/classes/App.class");
        Files.createDirectories(gitFile.getParent());
        Files.createDirectories(targetFile.getParent());
        Files.writeString(gitFile, "git");
        Files.writeString(targetFile, "compiled");
        Files.writeString(tempDirectory.resolve("pom.xml"), "<project/>");

        ProjectContext context = scanner.scan(tempDirectory);

        assertThat(context.files()).containsExactly(Path.of("pom.xml"));
    }

    @Test
    void rejectsMissingPath() {
        Path missingPath = tempDirectory.resolve("missing");

        assertThatThrownBy(() -> scanner.scan(missingPath))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Target path does not exist");
    }

    @Test
    void contextDefensivelyCopiesAndSortsFiles() {
        List<Path> files = new ArrayList<>(List.of(Path.of("z.txt"), Path.of("a.txt")));

        ProjectContext context = new ProjectContext(tempDirectory, files);
        files.clear();

        assertThat(context.files()).containsExactly(Path.of("a.txt"), Path.of("z.txt"));
        assertThatThrownBy(() -> context.files().add(Path.of("new.txt")))
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
