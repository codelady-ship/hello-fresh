package io.github.codeladyship.hellofresh.config;

import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DatabaseUrlEnvironmentPostProcessorTest {

    private final DatabaseUrlEnvironmentPostProcessor processor = new DatabaseUrlEnvironmentPostProcessor();

    @Test
    void convertsNeonStyleUrl() {
        var props = DatabaseUrlEnvironmentPostProcessor.convert(
                "postgresql://app_user:p%40ss@ep-cool-name-123.eu-central-1.aws.neon.tech/hellofresh?sslmode=require");

        assertThat(props)
                .containsEntry("JDBC_DATABASE_URL",
                        "jdbc:postgresql://ep-cool-name-123.eu-central-1.aws.neon.tech/hellofresh?sslmode=require")
                .containsEntry("JDBC_DATABASE_USERNAME", "app_user")
                .containsEntry("JDBC_DATABASE_PASSWORD", "p@ss");
    }

    @Test
    void addsSslModeAndKeepsPort() {
        var props = DatabaseUrlEnvironmentPostProcessor.convert("postgres://u:p@db.example.com:5433/app");

        assertThat(props).containsEntry("JDBC_DATABASE_URL", "jdbc:postgresql://db.example.com:5433/app?sslmode=require");
    }

    @Test
    void keepsPlusSignsInUserAndPassword() {
        var props = DatabaseUrlEnvironmentPostProcessor.convert("postgresql://app+user:a+b%2Bc%20d@h/db");

        assertThat(props)
                .containsEntry("JDBC_DATABASE_USERNAME", "app+user")
                .containsEntry("JDBC_DATABASE_PASSWORD", "a+b+c d");
    }

    @Test
    void failsClearlyWhenHostIsMissing() {
        // unencoded '/' in the password: URI parsing finds no host
        assertThatThrownBy(() -> DatabaseUrlEnvironmentPostProcessor.convert("postgresql://u:pa/ss@h/db"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no host");
        assertThatThrownBy(() -> DatabaseUrlEnvironmentPostProcessor.convert("postgresql:///db"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no host");
    }

    @Test
    void rejectsOtherSchemes() {
        assertThatThrownBy(() -> DatabaseUrlEnvironmentPostProcessor.convert("mysql://u:p@h/db"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void doesNothingWhenJdbcUrlIsAlreadySet() {
        MockEnvironment env = new MockEnvironment()
                .withProperty("DATABASE_URL", "postgresql://u:p@h/db")
                .withProperty("JDBC_DATABASE_URL", "jdbc:postgresql://other/db");

        processor.postProcessEnvironment(env, null);

        assertThat(env.getProperty("JDBC_DATABASE_URL")).isEqualTo("jdbc:postgresql://other/db");
    }

    @Test
    void exposesConvertedPropertiesToTheEnvironment() {
        MockEnvironment env = new MockEnvironment().withProperty("DATABASE_URL", "postgresql://u:p@h/db");

        processor.postProcessEnvironment(env, null);

        assertThat(env.getProperty("JDBC_DATABASE_URL")).isEqualTo("jdbc:postgresql://h/db?sslmode=require");
        assertThat(env.getProperty("JDBC_DATABASE_USERNAME")).isEqualTo("u");
    }
}
