package nemethi.xrate.plugin;

import org.apache.juneau.rest.client2.RestCallException;
import org.apache.juneau.rest.client2.RestClient;
import org.apache.juneau.rest.client2.RestRequest;
import org.apache.juneau.rest.client2.RestResponse;
import org.apache.juneau.rest.client2.RestResponseBody;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Currency;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExchangeRateApiClientTest {

    private static final String ENDPOINT_URI = "https://example.com";
    private static final Currency FROM = Currency.getInstance("USD");
    private static final Currency TO = Currency.getInstance("GBP");
    private static final BigDecimal AMOUNT = new BigDecimal("2");
    private static final String API_KEY = "testApiKey";
    private static final URI EXPECTED_URI = URI.create(String.format(
            "%s/v6/%s/pair/%s/%s/%s", ENDPOINT_URI, API_KEY, FROM, TO, AMOUNT));

    @Mock
    private RestClient restClient;
    @Mock
    private RestRequest request;
    @Mock
    private RestResponse response;
    @Mock
    private RestResponseBody responseBody;

    private ExchangeRateApiClient client;

    @BeforeEach
    void setUp() {
        client = new ExchangeRateApiClient(ENDPOINT_URI, restClient);
    }

    @Nested
    class HappyPath {

        @Test
        void convert() throws RestCallException, ExchangeRateApiException, URISyntaxException {
            final String expectedResult = "42";
            when(restClient.get(any())).thenReturn(request);
            when(request.run()).thenReturn(response);
            when(response.getBody()).thenReturn(responseBody);
            when(responseBody.as(any(Class.class), any(), any())).thenReturn(Map.of("result", "success", "conversion_result", expectedResult));

            BigDecimal result = client.convert(FROM, TO, AMOUNT, API_KEY);

            assertThat(result).isEqualTo(new BigDecimal(expectedResult));
            verify(restClient).get(EXPECTED_URI);
            verify(request).run();
            verify(response).getBody();
            verify(responseBody).as(Map.class, String.class, String.class);
        }
    }

    @Nested
    class UnhappyPath {

        @Test
        void throwsExceptionOnInvalidUri() {
            client = new ExchangeRateApiClient("[malformed", restClient);

            Throwable thrown = catchThrowable(() -> client.convert(FROM, TO, AMOUNT, API_KEY));

            assertThat(thrown).isInstanceOf(URISyntaxException.class);
        }

        @Test
        void throwsExceptionOnRestClientError() throws RestCallException {
            when(restClient.get(any())).thenReturn(request);
            when(request.run()).thenThrow(RestCallException.class);

            Throwable thrown = catchThrowable(() -> client.convert(FROM, TO, AMOUNT, API_KEY));

            assertThat(thrown).isInstanceOf(RestCallException.class);
            verify(restClient).get(EXPECTED_URI);
            verify(request).run();
        }

        @Test
        void throwsExceptionOnUnsuccessfulConversion() throws RestCallException {
            when(restClient.get(any())).thenReturn(request);
            when(request.run()).thenReturn(response);
            when(response.getBody()).thenReturn(responseBody);
            when(responseBody.as(any(Class.class), any(), any())).thenReturn(Map.of("result", "error", "error-type", "test"));

            Throwable thrown = catchThrowable(() -> client.convert(FROM, TO, AMOUNT, API_KEY));

            assertThat(thrown).isInstanceOf(ExchangeRateApiException.class);
            verify(restClient).get(EXPECTED_URI);
            verify(request).run();
            verify(response).getBody();
            verify(responseBody).as(Map.class, String.class, String.class);
        }

        @Test
        void doesNotHandleMissingConversionResult() throws RestCallException {
            when(restClient.get(any())).thenReturn(request);
            when(request.run()).thenReturn(response);
            when(response.getBody()).thenReturn(responseBody);
            when(responseBody.as(any(Class.class), any(), any())).thenReturn(Map.of("result", "success"));

            Throwable thrown = catchThrowable(() -> client.convert(FROM, TO, AMOUNT, API_KEY));

            assertThat(thrown)
                    .isInstanceOf(NullPointerException.class)
                    .hasStackTraceContaining("BigDecimal");
            verify(restClient).get(EXPECTED_URI);
            verify(request).run();
            verify(response).getBody();
            verify(responseBody).as(Map.class, String.class, String.class);
        }

        @Test
        void throwsExceptionOnMissingResponseBody() throws RestCallException {
            when(restClient.get(any())).thenReturn(request);
            when(request.run()).thenReturn(response);
            when(response.getBody()).thenReturn(responseBody);
            when(responseBody.as(any(Class.class), any(), any())).thenReturn(null);

            Throwable thrown = catchThrowable(() -> client.convert(FROM, TO, AMOUNT, API_KEY));

            assertThat(thrown).isInstanceOf(ExchangeRateApiException.class);
            verify(restClient).get(EXPECTED_URI);
            verify(request).run();
            verify(response).getBody();
            verify(responseBody).as(Map.class, String.class, String.class);
        }
    }
}
