package nemethi.xrate.core.integ;

import nemethi.xrate.api.CurrencyConverter;
import nemethi.xrate.core.PluginLoader;
import nemethi.xrate.core.integ.util.TestCurrencyConverter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class PluginLoaderIT {

    private static final Path MAVEN_TEST_CLASSES_DIRECTORY = Paths.get("target", "test-classes");
    private static final String META_INF_SERVICES_DIRECTORY = String.format("META-INF%sservices", File.separator);
    private static final String PROVIDER_CONFIG_FILENAME = "nemethi.xrate.api.CurrencyConverter";
    private static final List<String> PROVIDER_CONFIG_CONTENT = List.of("nemethi.xrate.core.integ.util.TestCurrencyConverter");

    private Path providerConfigFile;
    private Path servicesDirectory;
    private Path metaInfDirectory;

    @BeforeEach
    void setUp() throws IOException {
        providerConfigFile = createProviderConfigFile();
        servicesDirectory = providerConfigFile.getParent();
        metaInfDirectory = servicesDirectory.getParent();
    }

    private Path createProviderConfigFile() throws IOException {
        Path servicesDir = Files.createDirectories(MAVEN_TEST_CLASSES_DIRECTORY.resolve(META_INF_SERVICES_DIRECTORY));
        Path providerConfigFile = servicesDir.resolve(PROVIDER_CONFIG_FILENAME);
        Files.write(providerConfigFile, PROVIDER_CONFIG_CONTENT);
        return providerConfigFile;
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(providerConfigFile);
        Files.deleteIfExists(servicesDirectory);
        Files.deleteIfExists(metaInfDirectory);
    }

    @Test
    void loadsPluginFromClasspath() {
        PluginLoader loader = new PluginLoader();

        Optional<CurrencyConverter> plugin = loader.findFirstPlugin();

        assertThat(plugin).isPresent();
        CurrencyConverter converter = plugin.get();
        assertThat(converter).isInstanceOf(TestCurrencyConverter.class);
    }
}
