package nemethi.xrate.core;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static java.util.Objects.isNull;

public class Configuration {

    private static final String CURR_CONV_ENDPOINT = "https://free.currconv.com/api/v7/convert";
    private static final String EMPTY_STRING = "";
    private static final String CORE_ENDPOINT_KEY = "xrate.core.endpoint";
    private static final String CORE_AUTH_KEY = "xrate.core.auth";
    private static final String PLUGIN_AUTH_KEY = "xrate.plugin.auth";

    private final Properties properties;

    public Configuration(String configFilePath) throws ConfigurationException {
        try {
            properties = loadProperties(configFilePath);
        } catch (IOException e) {
            String message = String.format("Cannot load configuration: %s", configFilePath);
            throw new ConfigurationException(message, e);
        }
    }

    private Properties loadProperties(String configFilePath) throws IOException {
        Properties props = new Properties();
        InputStream configFile = findConfigFile(configFilePath);
        props.load(configFile);
        configFile.close();
        return props;
    }

    private InputStream findConfigFile(String configFilePath) throws FileNotFoundException {
        InputStream config = findConfigOnClasspath(configFilePath);
        if (isNull(config)) {
            return findConfigInFileSystem(configFilePath);
        }
        return config;
    }

    private InputStream findConfigOnClasspath(String configFilePath) {
        return getClass().getClassLoader().getResourceAsStream(configFilePath);
    }

    private InputStream findConfigInFileSystem(String configFilePath) throws FileNotFoundException {
        return new FileInputStream(configFilePath);
    }

    Configuration(Properties properties) {
        this.properties = properties;
    }

    public String getCurrConvEndpoint() {
        return properties.getProperty(CORE_ENDPOINT_KEY, CURR_CONV_ENDPOINT);
    }

    public String getCoreAuthCredentials() {
        return properties.getProperty(CORE_AUTH_KEY, EMPTY_STRING);
    }

    public String getPluginAuthCredentials() {
        return properties.getProperty(PLUGIN_AUTH_KEY, EMPTY_STRING);
    }
}
