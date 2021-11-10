package nemethi.xrate.plugin.integ;

import nemethi.xrate.plugin.ExchangeRateApiClient;
import nemethi.xrate.plugin.ExchangeRateApiException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.apache.juneau.json.JsonSerializer;
import org.apache.juneau.rest.client2.RestCallException;
import org.apache.juneau.serializer.SerializeException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.util.Currency;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

class ExchangeRateApiClientIT {

    private static final Currency FROM = Currency.getInstance("USD");
    private static final Currency TO = Currency.getInstance("GBP");
    private static final BigDecimal AMOUNT = new BigDecimal("2");
    private static final String API_KEY = "testApiKey";
    private static final String EXPECTED_RESULT = "42.24";
    private static final double MOCK_RESULT = Double.parseDouble(EXPECTED_RESULT);

    private static MockWebServer mockWebServer;
    private ExchangeRateApiClient client;

    @BeforeAll
    static void beforeAll() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @AfterAll
    static void afterAll() throws IOException {
        mockWebServer.shutdown();
    }

    @BeforeEach
    void setUp() {
        String endpointUri = String.format("http://localhost:%s", mockWebServer.getPort());
        client = new ExchangeRateApiClient(endpointUri);
    }

    @Test
    void successResponse() throws SerializeException, RestCallException, ExchangeRateApiException, URISyntaxException, InterruptedException {
        final Map<String, Object> responseBody = Map.of("result", "success", "conversion_result", MOCK_RESULT);
        mockWebServer.enqueue(mockResponse(responseBody));

        BigDecimal result = client.convert(FROM, TO, AMOUNT, API_KEY);

        assertThat(result).isEqualTo(new BigDecimal(EXPECTED_RESULT));
        verifyRequest(mockWebServer.takeRequest());
    }

    @Test
    void errorResponse() throws SerializeException, InterruptedException {
        final Map<String, Object> responseBody = Map.of("result", "error", "error-type", "invalid-api-key");
        mockWebServer.enqueue(mockResponse(400, responseBody));

        Throwable thrown = catchThrowable(() -> client.convert(FROM, TO, AMOUNT, API_KEY));

        assertThat(thrown).isInstanceOf(ExchangeRateApiException.class);
        verifyRequest(mockWebServer.takeRequest());
    }

    @Test
    void noResponseBody() throws InterruptedException {
        mockWebServer.enqueue(new MockResponse().setResponseCode(500));

        Throwable thrown = catchThrowable(() -> client.convert(FROM, TO, AMOUNT, API_KEY));

        assertThat(thrown).isInstanceOf(ExchangeRateApiException.class);
        verifyRequest(mockWebServer.takeRequest());
    }

    private MockResponse mockResponse(Map<String, Object> responseBody) throws SerializeException {
        return mockResponse(200, responseBody);
    }

    private MockResponse mockResponse(int statusCode, Map<String, Object> responseBody) throws SerializeException {
        return new MockResponse()
                .setResponseCode(statusCode)
                .setBody(asJson(responseBody))
                .setHeader("Content-Type", "application/json");
    }

    private String asJson(Map<String, Object> responseBody) throws SerializeException {
        JsonSerializer serializer = JsonSerializer.create().build();
        return serializer.serialize(responseBody);
    }

    private void verifyRequest(RecordedRequest request) {
        assertThat(request.getMethod()).isEqualTo("GET");
        String pathSegment = String.format("/v6/%s/pair/%s/%s/%s", API_KEY, FROM, TO, AMOUNT);
        assertThat(request.getPath()).contains(pathSegment);
    }
}
