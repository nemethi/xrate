package nemethi.xrate.core.integ;

import nemethi.xrate.core.CurrConvApiClient;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class CurrConvApiClientIT {

    private static final String FROM_CURRENCY = "USD";
    private static final String TO_CURRENCY = "GBP";
    private static final BigDecimal EXPECTED_RATE = new BigDecimal("0.71584823");
    private static final String API_KEY = "testApiKey";
    private static final String CONTENT_TYPE_HEADER_KEY = "Content-Type";
    private static final String APPLICATION_JSON_HEADER_VALUE = "application/json";

    private static MockWebServer mockWebServer;
    private CurrConvApiClient client;

    @BeforeAll
    static void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @BeforeEach
    void initialize() {
        client = new CurrConvApiClient(Currency.getInstance(FROM_CURRENCY), Currency.getInstance(TO_CURRENCY), API_KEY);
        client.setEndpointUri(String.format("http://localhost:%s", mockWebServer.getPort()));
    }

    @Nested
    class HappyPath {

        @Test
        void clientReturnsConversionRate() throws InterruptedException {
            mockWebServer.enqueue(mockResponse());

            Optional<BigDecimal> result = client.getConversionRate();

            assertThat(result)
                    .isPresent()
                    .hasValue(EXPECTED_RATE);
            verifyRequest(mockWebServer.takeRequest());
        }

        private MockResponse mockResponse() {
            return new MockResponse()
                    .setBody(mockBody())
                    .setHeader(CONTENT_TYPE_HEADER_KEY, APPLICATION_JSON_HEADER_VALUE);
        }

        private String mockBody() {
            String key = String.format("%s_%s", FROM_CURRENCY, TO_CURRENCY);
            return new JSONObject().put(key, EXPECTED_RATE).toString();
        }
    }

    @Nested
    class UnhappyPaths {

        @Test
        @DisplayName("When the server returns an error then the result is not present")
        void serverErrorResponse() throws InterruptedException {
            mockWebServer.enqueue(new MockResponse().setResponseCode(500));

            Optional<BigDecimal> result = client.getConversionRate();

            assertThat(result).isNotPresent();
            verifyRequest(mockWebServer.takeRequest());
        }

        @Test
        @DisplayName("When the returned JSON doesn't contain the rate then the result is not present")
        void invalidResponse() throws InterruptedException {
            mockWebServer.enqueue(mockResponse());

            Optional<BigDecimal> result = client.getConversionRate();

            assertThat(result).isNotPresent();
            verifyRequest(mockWebServer.takeRequest());
        }

        private MockResponse mockResponse() {
            return new MockResponse()
                    .setBody("{}")
                    .setHeader(CONTENT_TYPE_HEADER_KEY, APPLICATION_JSON_HEADER_VALUE);
        }
    }

    private void verifyRequest(RecordedRequest request) {
        assertThat(request.getMethod()).isEqualTo("GET");
        String queryParam = String.format("q=%s_%s", FROM_CURRENCY, TO_CURRENCY);
        String apiKeyParam = String.format("apiKey=%s", API_KEY);
        String compactParam = "compact=ultra";
        assertThat(request.getPath()).contains(queryParam, apiKeyParam, compactParam);
    }
}
