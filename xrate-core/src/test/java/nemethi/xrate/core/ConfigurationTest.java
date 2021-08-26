package nemethi.xrate.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

class ConfigurationTest {

    private static final String CURR_CONV_ENDPOINT = "https://free.currconv.com/api/v7/convert";
    private static final String CORE_AUTH_KEY = "xrate.core.auth";
    private static final String CORE_AUTH_CREDS = "testCoreAuthCreds";
    private static final String CORE_ENDPOINT_KEY = "xrate.core.endpoint";
    private static final String CORE_ENDPOINT = "testCoreEndpoint";

    private Configuration config;
    private Properties properties;

    @BeforeEach
    void setUp() {
        properties = new Properties();
        properties.setProperty(CORE_ENDPOINT_KEY, CORE_ENDPOINT);
        properties.setProperty(CORE_AUTH_KEY, CORE_AUTH_CREDS);
        config = new Configuration(properties);
    }

    @Test
    void getCurrConvEndpoint() {
        assertThat(config.getCurrConvEndpoint()).isEqualTo(CORE_ENDPOINT);
    }

    @Test
    void getCurrConvEndpointReturnsAPredefinedEndpointByDefault() {
        properties.remove(CORE_ENDPOINT_KEY);
        assertThat(config.getCurrConvEndpoint()).isEqualTo(CURR_CONV_ENDPOINT);
    }

    @Test
    void getCoreAuthCredentials() {
        assertThat(config.getCoreAuthCredentials()).isEqualTo(CORE_AUTH_CREDS);
    }

    @Test
    void getCoreAuthCredentialsReturnsEmptyStringByDefault() {
        properties.remove(CORE_AUTH_KEY);
        assertThat(config.getCoreAuthCredentials()).isEmpty();
    }
}
