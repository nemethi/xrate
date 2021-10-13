package nemethi.xrate.core.integ.util;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class ProviderConfigExtension implements BeforeEachCallback, AfterEachCallback {

    private static final Path MAVEN_TEST_CLASSES_DIRECTORY = Paths.get("target", "test-classes");
    private static final String META_INF_SERVICES_DIRECTORY = String.format("META-INF%sservices", File.separator);
    private static final String PROVIDER_CONFIG_FILENAME = "nemethi.xrate.api.CurrencyConverter";
    private static final List<String> PROVIDER_CONFIG_CONTENT = List.of("nemethi.xrate.core.integ.util.TestCurrencyConverter");

    private Path providerConfigFile;
    private Path servicesDirectory;
    private Path metaInfDirectory;

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
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

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        Files.deleteIfExists(providerConfigFile);
        Files.deleteIfExists(servicesDirectory);
        Files.deleteIfExists(metaInfDirectory);
    }
}
