package nemethi.xrate.core.integ;

import nemethi.xrate.core.Configuration;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;

class ConfigurationIT {

    private static final String CORE_AUTH_CREDS = "testCoreAuthCreds";
    private static final String CONFIG_FILENAME = "/config-it.properties";
    private static final String MISSING_CONFIG_FILENAME = "/missing-config.properties";
    private static final String ERROR_MESSAGE = String.format("Cannot load configuration: %s", MISSING_CONFIG_FILENAME);
    private static final String ROOT_CAUSE_ERROR_MESSAGE = String.format("Resource %s is not found", MISSING_CONFIG_FILENAME);

    @Test
    void loadsConfigFromClasspath() {
        Configuration config = new Configuration(CONFIG_FILENAME);
        assertThat(config.getCoreAuthCredentials()).isEqualTo(CORE_AUTH_CREDS);
    }

    @Test
    void constructorThrowsExceptionOnError() {
        Throwable thrown = catchThrowable(() -> new Configuration(MISSING_CONFIG_FILENAME));

        assertThat(thrown)
                .isInstanceOf(UncheckedIOException.class)
                .hasMessage(ERROR_MESSAGE)
                .hasRootCauseInstanceOf(IOException.class)
                .hasRootCauseMessage(ROOT_CAUSE_ERROR_MESSAGE, MISSING_CONFIG_FILENAME);
    }
}
