package dev.aetlas.bootdoctor.rules;

import static org.assertj.core.api.Assertions.assertThat;

import dev.aetlas.bootdoctor.context.ProjectContext;
import dev.aetlas.bootdoctor.context.ProjectScanner;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CorsWildcardRuleTest {

    @TempDir Path tempDirectory;

    private final CorsWildcardRule rule = new CorsWildcardRule();

    @Test
    void detectsDefaultAndMultilineWildcards() throws Exception {
        ProjectContext defaultWildcard =
                contextWithSource(
                        """
                @CrossOrigin
                class DefaultCorsController {}
                """);
        ProjectContext explicitWildcard =
                contextWithSource(
                        """
                @CrossOrigin(
                    origins = "*"
                )
                class OpenController {}
                """);

        assertThat(rule.evaluate(defaultWildcard)).hasSize(1);
        assertThat(rule.evaluate(explicitWildcard)).hasSize(1);
    }

    @Test
    void acceptsExplicitRestrictedOrigin() throws Exception {
        ProjectContext context =
                contextWithSource(
                        """
                @CrossOrigin(origins = "https://example.com")
                class RestrictedController {}
                """);

        assertThat(rule.evaluate(context)).isEmpty();
    }

    @Test
    void ignoresCommentedOutWildcard() throws Exception {
        ProjectContext context =
                contextWithSource(
                        """
                // @CrossOrigin(origins = "*")
                /*
                 * @CrossOrigin
                 */
                class ControllerWithoutCors {}
                """);

        assertThat(rule.evaluate(context)).isEmpty();
    }

    private ProjectContext contextWithSource(String source) throws Exception {
        Path project = Files.createTempDirectory(tempDirectory, "cors-project-");
        Path javaFile = project.resolve("src/main/java/example/Controller.java");
        Files.createDirectories(javaFile.getParent());
        Files.writeString(javaFile, source);
        return new ProjectScanner().scan(project);
    }
}
