package dev.aetlas.bootdoctor.rules;

import static org.assertj.core.api.Assertions.assertThat;

import dev.aetlas.bootdoctor.context.ProjectContext;
import dev.aetlas.bootdoctor.context.ProjectScanner;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PlainSecretRuleTest {

    @TempDir Path tempDirectory;

    private final PlainSecretRule rule = new PlainSecretRule();

    @Test
    void ignoresEnvironmentReferencesAndEncryptedValues() throws Exception {
        ProjectContext context =
                contextWithConfig(
                        """
                password: ${DB_PASSWORD}
                client-secret: ${CLIENT_SECRET:}
                api-key: ENC(encrypted-value)
                token: vault://service/token
                """);

        assertThat(rule.evaluate(context)).isEmpty();
    }

    @Test
    void detectsLiteralAndPlaceholderDefaultSecretsWithoutLeakingValues() throws Exception {
        ProjectContext context =
                contextWithConfig(
                        """
                password: local-password
                client-secret: ${CLIENT_SECRET:unsafe-default}
                """);

        assertThat(rule.evaluate(context))
                .hasSize(2)
                .allMatch(finding -> !finding.message().contains("local-password"))
                .allMatch(finding -> !finding.message().contains("unsafe-default"));
    }

    private ProjectContext contextWithConfig(String content) throws Exception {
        Path config = tempDirectory.resolve("src/main/resources/application.yml");
        Files.createDirectories(config.getParent());
        Files.writeString(config, content);
        return new ProjectScanner().scan(tempDirectory);
    }
}
