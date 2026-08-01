package dev.aetlas.bootdoctor.context;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PomInspectorTest {

    @TempDir Path tempDirectory;

    private final ProjectScanner scanner = new ProjectScanner();
    private final PomInspector inspector = new PomInspector();

    @Test
    void detectsSpringBootFromPlugin() throws Exception {
        Files.writeString(
                tempDirectory.resolve("pom.xml"),
                """
                <project xmlns="http://maven.apache.org/POM/4.0.0">
                  <modelVersion>4.0.0</modelVersion>
                  <build><plugins><plugin>
                    <groupId>org.springframework.boot</groupId>
                    <artifactId>spring-boot-maven-plugin</artifactId>
                  </plugin></plugins></build>
                </project>
                """);

        PomInfo info = inspector.inspect(scanner.scan(tempDirectory));

        assertThat(info.validMavenProject()).isTrue();
        assertThat(info.springBootProject()).isTrue();
    }

    @Test
    void rejectsDoctypeAndExternalEntityDeclarations() throws Exception {
        Files.writeString(
                tempDirectory.resolve("pom.xml"),
                """
                <!DOCTYPE project [<!ENTITY xxe SYSTEM "file:///etc/passwd">]>
                <project><name>&xxe;</name></project>
                """);

        PomInfo info = inspector.inspect(scanner.scan(tempDirectory));

        assertThat(info.validMavenProject()).isFalse();
        assertThat(info.parseError())
                .hasValueSatisfying(message -> assertThat(message).contains("safely"));
    }
}
