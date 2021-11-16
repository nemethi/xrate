package nemethi.xrate.core.integ;

import com.github.stefanbirkner.systemlambda.Statement;
import nemethi.xrate.core.Application;
import nemethi.xrate.core.integ.util.NullResultCurrencyConverter;
import nemethi.xrate.core.integ.util.ProviderConfigExtension;
import nemethi.xrate.core.integ.util.TestCurrencyConverter;
import nemethi.xrate.core.integ.util.UseConverter;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;

import static com.github.stefanbirkner.systemlambda.SystemLambda.catchSystemExit;
import static org.assertj.core.api.Assertions.assertThat;

class ApplicationIT {

    private static final Pattern AVAILABLE_CURRENCIES_PATTERN = Pattern.compile("^\\w{3}(, \\w{3})*\\r?\\n$");
    private static final Pattern INVALID_CONFIG_PATTERN = Pattern.compile("xrate: Cannot load configuration: \\w+\\r?\\n.*", Pattern.DOTALL);
    private static final String INVALID_VALUE = "INVALID";
    private static final String SPECIFIED_CONFIG_FILENAME = "specified-config.properties";
    private static final String PLUGIN_API_KEY = "testPluginAPIKey";
    private static final String PLUGIN_API_KEY_2 = "testPluginAPIKey2";
    private static final String PLUGIN_AUTH_CONFIG_LINE = String.format("xrate.plugin.auth=%s", PLUGIN_API_KEY_2);
    private static final String CORE_API_KEY = "testAPIKey";
    private static final String CORE_API_KEY_2 = "testAPIKey2";
    private static final String CORE_AUTH_CONFIG_LINE = String.format("xrate.core.auth=%s", CORE_API_KEY_2);
    private static final String CORE_ENDPOINT_CONFIG_LINE = "xrate.core.endpoint=http://localhost:2552";
    private static final String USAGE = "Usage: xrate [-hlV] [-c=<configFilePath>] FROM TO AMOUNT";
    private static final String DESCRIPTION = "Get exchange rates and convert currencies using third-party services.";
    private static final String AMOUNT = "757.57";
    private static final String EXPECTED_OUTPUT_OF_DEFAULT_CONVERSION = "1 USD = 2 GBP\n1 USD = 2 GBP\n1 GBP = 0.5 USD\n";
    private static final String EXPECTED_OUTPUT_OF_SPECIFIC_CONVERSION = AMOUNT + " EUR = 1,515.14 HUF\n1 EUR = 2 HUF\n1 HUF = 0.5 EUR\n";
    private static final String MISSING_RESULT_ERROR_MESSAGE = "xrate: Error: converter did not return any result\n";

    private ByteArrayOutputStream systemOut;
    private ByteArrayOutputStream systemErr;
    private PrintStream originalSystemOut;
    private PrintStream originalSystemErr;

    @BeforeEach
    void setUp() {
        redirectSystemOut();
        redirectSystemErr();
    }

    private void redirectSystemOut() {
        systemOut = new ByteArrayOutputStream();
        originalSystemOut = System.out;
        PrintStream newOut = new PrintStream(systemOut, true);
        System.setOut(newOut);
    }

    private void redirectSystemErr() {
        systemErr = new ByteArrayOutputStream();
        originalSystemErr = System.err;
        PrintStream newErr = new PrintStream(systemErr, true);
        System.setErr(newErr);
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalSystemOut);
        System.setErr(originalSystemErr);
    }

    @Nested
    @ExtendWith(ProviderConfigExtension.class)
    class ConvertWithPlugin {

        @Test
        void convertUsingDefaultValues() throws Exception {
            Statement statement = () -> Application.main(args());

            assertExitCode(statement, 0);
            assertThat(systemOut.toString()).isEqualToNormalizingNewlines(EXPECTED_OUTPUT_OF_DEFAULT_CONVERSION);
            assertThat(TestCurrencyConverter.getAuthCredentials()).isEqualTo(PLUGIN_API_KEY);
        }

        @Test
        void convertWithSpecifiedValues() throws Exception {
            Statement statement = () -> Application.main(args("EUR", "HUF", AMOUNT));

            assertExitCode(statement, 0);
            assertThat(systemOut.toString()).isEqualToNormalizingNewlines(EXPECTED_OUTPUT_OF_SPECIFIC_CONVERSION);
            assertThat(TestCurrencyConverter.getAuthCredentials()).isEqualTo(PLUGIN_API_KEY);
        }

        @Test
        void convertUsingDefaultValuesWithSpecifiedConfig(@TempDir Path tempDir) throws Exception {
            Path configFile = tempDir.resolve(SPECIFIED_CONFIG_FILENAME);
            Files.write(configFile, List.of(PLUGIN_AUTH_CONFIG_LINE));

            Statement statement = () -> Application.main(args("-c", configFile.toAbsolutePath().toString()));
            assertExitCode(statement, 0);
            assertThat(systemOut.toString()).isEqualToNormalizingNewlines(EXPECTED_OUTPUT_OF_DEFAULT_CONVERSION);
            assertThat(TestCurrencyConverter.getAuthCredentials()).isEqualTo(PLUGIN_API_KEY_2);
        }

        @Test
        void convertUsingSpecifiedValuesWithSpecifiedConfig(@TempDir Path tempDir) throws Exception {
            Path configFile = tempDir.resolve(SPECIFIED_CONFIG_FILENAME);
            Files.write(configFile, List.of(PLUGIN_AUTH_CONFIG_LINE));

            Statement statement = () -> Application.main(args("EUR", "HUF", AMOUNT, "--config", configFile.toAbsolutePath().toString()));

            assertExitCode(statement, 0);
            assertThat(systemOut.toString()).isEqualToNormalizingNewlines(EXPECTED_OUTPUT_OF_SPECIFIC_CONVERSION);
            assertThat(TestCurrencyConverter.getAuthCredentials()).isEqualTo(PLUGIN_API_KEY_2);
        }

        @Test
        @UseConverter(NullResultCurrencyConverter.class)
        void convertWithPluginThatDoesNotObeyApiContract() throws Exception {
            Statement statement = () -> Application.main(args());

            assertExitCode(statement, 1);
            assertThat(systemErr.toString()).isEqualToNormalizingNewlines(MISSING_RESULT_ERROR_MESSAGE);
            assertThat(systemOut.toString()).isEmpty();
        }
    }

    @Nested
    class ConvertWithDefaultConverter {

        private MockWebServer mockWebServer;

        @BeforeEach
        void setUp() throws IOException {
            mockWebServer = new MockWebServer();
            mockWebServer.start(2552);
        }

        @AfterEach
        void tearDown() throws IOException {
            mockWebServer.shutdown();
        }

        @Test
        void convertUsingDefaultValues() throws Exception {
            mockWebServer.enqueue(mockResponseFor("USD", "GBP"));
            Statement statement = () -> Application.main(args());

            assertExitCode(statement, 0);
            assertThat(systemOut.toString()).isEqualToNormalizingNewlines(EXPECTED_OUTPUT_OF_DEFAULT_CONVERSION);
            verifyRequest(mockWebServer.takeRequest(), "USD", "GBP", CORE_API_KEY);
        }

        @Test
        void convertWithSpecifiedValues() throws Exception {
            mockWebServer.enqueue(mockResponseFor("EUR", "HUF"));
            Statement statement = () -> Application.main(args("EUR", "HUF", AMOUNT));

            assertExitCode(statement, 0);
            assertThat(systemOut.toString()).isEqualToNormalizingNewlines(EXPECTED_OUTPUT_OF_SPECIFIC_CONVERSION);
            verifyRequest(mockWebServer.takeRequest(), "EUR", "HUF", CORE_API_KEY);
        }

        @Test
        void convertUsingDefaultValuesWithSpecifiedConfig(@TempDir Path tempDir) throws Exception {
            mockWebServer.enqueue(mockResponseFor("USD", "GBP"));
            Path configFile = tempDir.resolve(SPECIFIED_CONFIG_FILENAME);
            Files.write(configFile, List.of(CORE_ENDPOINT_CONFIG_LINE, CORE_AUTH_CONFIG_LINE));

            Statement statement = () -> Application.main(args("-c", configFile.toAbsolutePath().toString()));
            assertExitCode(statement, 0);
            assertThat(systemOut.toString()).isEqualToNormalizingNewlines(EXPECTED_OUTPUT_OF_DEFAULT_CONVERSION);
            verifyRequest(mockWebServer.takeRequest(), "USD", "GBP", CORE_API_KEY_2);
        }

        @Test
        void convertUsingSpecifiedValuesWithSpecifiedConfig(@TempDir Path tempDir) throws Exception {
            mockWebServer.enqueue(mockResponseFor("EUR", "HUF"));
            Path configFile = tempDir.resolve(SPECIFIED_CONFIG_FILENAME);
            Files.write(configFile, List.of(CORE_ENDPOINT_CONFIG_LINE, CORE_AUTH_CONFIG_LINE));

            Statement statement = () -> Application.main(args("EUR", "HUF", AMOUNT, "--config", configFile.toAbsolutePath().toString()));

            assertExitCode(statement, 0);
            assertThat(systemOut.toString()).isEqualToNormalizingNewlines(EXPECTED_OUTPUT_OF_SPECIFIC_CONVERSION);
            verifyRequest(mockWebServer.takeRequest(), "EUR", "HUF", CORE_API_KEY_2);
        }

        @Test
        void conversionError() throws Exception {
            mockWebServer.enqueue(mockErrorResponse());
            Statement statement = () -> Application.main(args());

            assertExitCode(statement, 1);
            assertThat(systemErr.toString()).isEqualToNormalizingNewlines("xrate: Could not convert currency using default third-party API.\n");
            assertThat(systemOut.toString()).isEmpty();
            verifyRequest(mockWebServer.takeRequest(), "USD", "GBP", CORE_API_KEY);
        }

        private MockResponse mockResponseFor(String fromCurrency, String toCurrency) {
            return new MockResponse()
                    .setBody(mockBodyFor(fromCurrency, toCurrency))
                    .setHeader("Content-Type", "application/json");
        }

        private String mockBodyFor(String fromCurrency, String toCurrency) {
            String key = String.format("%s_%s", fromCurrency, toCurrency);
            return new JSONObject().put(key, "2").toString();
        }

        private MockResponse mockErrorResponse() {
            return new MockResponse().setResponseCode(500);
        }

        private void verifyRequest(RecordedRequest request, String fromCurrency, String toCurrency, String apiKey) {
            assertThat(request.getMethod()).isEqualTo("GET");
            String queryParam = String.format("q=%s_%s", fromCurrency, toCurrency);
            String apiKeyParam = String.format("apiKey=%s", apiKey);
            String compactParam = "compact=ultra";
            assertThat(request.getPath()).contains(queryParam, apiKeyParam, compactParam);
        }
    }

    @Nested
    class CommandLineTests {

        @Test
        void printAvailableCurrencies() throws Exception {
            printAvailableCurrencies(() -> Application.main(args("-l")));
            systemOut.reset();
            printAvailableCurrencies(() -> Application.main(args("--list")));
        }

        private void printAvailableCurrencies(Statement statement) throws Exception {
            assertExitCode(statement, 0);
            assertThat(systemOut.toString()).matches(AVAILABLE_CURRENCIES_PATTERN);
        }

        @Test
        void invalidFromParameter() throws Exception {
            Statement statement = () -> Application.main(args(INVALID_VALUE, "GBP", "2"));

            assertExitCode(statement, 2);
            assertThat(systemErr).hasToString(String.format("xrate: Unknown currency: %s%n", INVALID_VALUE));
        }

        @Test
        void invalidToParameter() throws Exception {
            Statement statement = () -> Application.main(args("USD", INVALID_VALUE, "2"));

            assertExitCode(statement, 2);
            assertThat(systemErr).hasToString(String.format("xrate: Unknown currency: %s%n", INVALID_VALUE));
        }

        @Test
        void invalidAmountParameter() throws Exception {
            Statement statement = () -> Application.main(args("USD", "GBP", INVALID_VALUE));

            assertExitCode(statement, 2);
            assertThat(systemErr).hasToString(String.format("xrate: Invalid amount: %s%n", INVALID_VALUE));
        }

        @Test
        void invalidConfigPath() throws Exception {
            invalidConfigPath(() -> Application.main(args("-c", INVALID_VALUE)));
            systemErr.reset();
            invalidConfigPath(() -> Application.main(args("--config", INVALID_VALUE)));
        }

        private void invalidConfigPath(Statement statement) throws Exception {
            assertExitCode(statement, 1);
            assertThat(systemErr.toString()).matches(INVALID_CONFIG_PATTERN);
        }

        @Test
        void printHelp() throws Exception {
            printHelp(() -> Application.main(args("-h")));
            systemOut.reset();
            printHelp(() -> Application.main(args("--help")));
        }

        private void printHelp(Statement statement) throws Exception {
            assertExitCode(statement, 0);
            assertThat(systemOut.toString()).contains(USAGE, DESCRIPTION, "FROM", "TO", "AMOUNT", "-c",
                    "--config=<configFilePath>", "-h", "--help", "-l", "--list", "-V", "--version");
        }

        @Test
        void printVersion() throws Exception {
            printVersion(() -> Application.main(args("-V")));
            systemOut.reset();
            printVersion(() -> Application.main(args("--version")));
        }

        private void printVersion(Statement statement) throws Exception {
            assertExitCode(statement, 0);
            assertThat(systemOut.toString()).isEqualToNormalizingNewlines("1.0-SNAPSHOT\n");
        }
    }

    private String[] args(String... args) {
        return args;
    }

    private void assertExitCode(Statement statement, int exitCode) throws Exception {
        assertThat(catchSystemExit(statement)).isEqualTo(exitCode);
    }
}
