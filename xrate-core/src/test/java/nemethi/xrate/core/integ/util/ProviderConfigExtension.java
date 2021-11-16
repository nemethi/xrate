package nemethi.xrate.core.integ.util;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.util.Objects.isNull;

public class ProviderConfigExtension implements BeforeEachCallback, AfterEachCallback {

    private static final Path MAVEN_TEST_CLASSES_DIRECTORY = Paths.get("target", "test-classes");
    private static final String META_INF_SERVICES_DIRECTORY = String.format("META-INF%sservices", File.separator);
    private static final String PROVIDER_CONFIG_FILENAME = "nemethi.xrate.api.CurrencyConverter";

    private Path providerConfigFile;
    private Path servicesDirectory;
    private Path metaInfDirectory;

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        providerConfigFile = createProviderConfigFile(context.getRequiredTestMethod());
        servicesDirectory = providerConfigFile.getParent();
        metaInfDirectory = servicesDirectory.getParent();
    }

    private Path createProviderConfigFile(Method testMethod) throws IOException {
        Path servicesDir = Files.createDirectories(MAVEN_TEST_CLASSES_DIRECTORY.resolve(META_INF_SERVICES_DIRECTORY));
        Path providerConfigFile = servicesDir.resolve(PROVIDER_CONFIG_FILENAME);
        Files.write(providerConfigFile, getConverterName(testMethod));
        return providerConfigFile;
    }

    private byte[] getConverterName(Method testMethod) {
        UseConverter annotation = testMethod.getAnnotation(UseConverter.class);
        if (isNull(annotation)) {
            return TestCurrencyConverter.class.getName().getBytes(StandardCharsets.UTF_8);
        }
        return annotation.value().getName().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        Files.deleteIfExists(providerConfigFile);
        Files.deleteIfExists(servicesDirectory);
        Files.deleteIfExists(metaInfDirectory);
    }
}
