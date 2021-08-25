package nemethi.xrate.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Properties;

import static java.util.Objects.isNull;

public class Configuration {

    private static final String CURR_CONV_ENDPOINT = "https://free.currconv.com/api/v7/convert";
    private static final String EMPTY_STRING = "";
    private static final String CORE_AUTH_KEY = "xrate.core.auth";

    private final Properties properties;

    public Configuration(String configFilename) {
        properties = new Properties();
        try {
            properties.load(getResourceAsStream(configFilename));
        } catch (IOException e) {
            String message = String.format("Cannot load configuration: %s", configFilename);
            throw new UncheckedIOException(message, e);
        }
    }

    private InputStream getResourceAsStream(String resource) throws IOException {
        InputStream stream = getClass().getResourceAsStream(resource);
        if (isNull(stream)) {
            throw new IOException(String.format("Resource %s is not found", resource));
        }
        return stream;
    }

    Configuration(Properties properties) {
        this.properties = properties;
    }

    public String getCurrConvEndpoint() {
        return CURR_CONV_ENDPOINT;
    }

    public String getCoreAuthCredentials() {
        return properties.getProperty(CORE_AUTH_KEY, EMPTY_STRING);
    }
}
