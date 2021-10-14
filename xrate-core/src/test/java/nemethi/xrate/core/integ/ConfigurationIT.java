package nemethi.xrate.core.integ;

import nemethi.xrate.core.Configuration;
import nemethi.xrate.core.ConfigurationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;

class ConfigurationIT {

    private static final String CORE_AUTH_KEY = "xrate.core.auth";
    private static final String CORE_AUTH_CREDS = "testCoreAuthCreds";
    private static final String CORE_AUTH_CREDS_2 = "testCoreAuthCreds2";
    private static final String CORE_AUTH_CONFIG_LINE = String.format("%s=%s", CORE_AUTH_KEY, CORE_AUTH_CREDS_2);
    private static final String CORE_ENDPOINT = "testCoreEndpoint";
    private static final String CURR_CONV_ENDPOINT = "https://free.currconv.com/api/v7/convert";
    private static final String PLUGIN_AUTH_CREDS = "testPluginAuthCreds";
    private static final String CONFIG_FILENAME = "config-it.properties";
    private static final String CONFIG_FILENAME_2 = "config-it2.properties";
    private static final String MISSING_CONFIG_FILENAME = "missing-config.properties";
    private static final String ERROR_MESSAGE = "Cannot load configuration: %s";

    @Test
    void loadsConfigFromClasspath() {
        Configuration config = new Configuration(CONFIG_FILENAME);
        assertThat(config.getCurrConvEndpoint()).isEqualTo(CORE_ENDPOINT);
        assertThat(config.getCoreAuthCredentials()).isEqualTo(CORE_AUTH_CREDS);
        assertThat(config.getPluginAuthCredentials()).isEqualTo(PLUGIN_AUTH_CREDS);
    }

    @Test
    void loadsConfigFromFileSystemIfNotOnClasspath(@TempDir Path tempDir) throws IOException {
        Path configFile = tempDir.resolve(CONFIG_FILENAME_2);
        Files.write(configFile, List.of(CORE_AUTH_CONFIG_LINE));

        Configuration config = new Configuration(configFile.toAbsolutePath().toString());
        assertThat(config.getCurrConvEndpoint()).isEqualTo(CURR_CONV_ENDPOINT);
        assertThat(config.getCoreAuthCredentials()).isEqualTo(CORE_AUTH_CREDS_2);
    }

    @Test
    void constructorThrowsExceptionOnError() {
        Throwable thrown = catchThrowable(() -> new Configuration(MISSING_CONFIG_FILENAME));

        assertThat(thrown)
                .isInstanceOf(ConfigurationException.class)
                .hasMessage(ERROR_MESSAGE, MISSING_CONFIG_FILENAME)
                .hasRootCauseInstanceOf(FileNotFoundException.class);
        assertThat(thrown.getCause()).hasMessageContaining(MISSING_CONFIG_FILENAME);
    }
}
