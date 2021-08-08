package nemethi.xrate.core;

import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.Currency;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("unchecked")
@ExtendWith(MockitoExtension.class)
class CurrConvApiClientTest {

    private static final Currency FROM = Currency.getInstance("USD");
    private static final Currency TO = Currency.getInstance("GBP");
    private static final String API_KEY = "testApiKey";
    private static final String URI_TEMPLATE = "https://free.currconv.com/api/v7/convert?q=%s_%s&apiKey=%s&compact=ultra";
    private static final URI EXPECTED_URI = URI.create(String.format(URI_TEMPLATE, FROM, TO, API_KEY));

    @Mock
    private HttpClient httpClient;
    @Mock
    private HttpResponse httpResponse;
    @Captor
    private ArgumentCaptor<HttpRequest> requestCaptor;

    private CurrConvApiClient client;

    @BeforeEach
    void setUp() {
        client = new CurrConvApiClient(FROM, TO, API_KEY);
        client.setHttpClient(httpClient);
    }

    @Test
    void getConversionRateFromThirdParty() throws Exception {
        var key = String.format("%s_%s", FROM, TO);
        var expectedRate = "0.756";
        var responseBody = new JSONObject().put(key, expectedRate).toString();
        when(httpClient.send(any(), any())).thenReturn(httpResponse);
        when(httpResponse.body()).thenReturn(responseBody);

        Optional<BigDecimal> result = client.getConversionRate();

        assertThat(result).isPresent().hasValue(new BigDecimal(expectedRate));
        verify(httpClient).send(requestCaptor.capture(), eq(BodyHandlers.ofString()));
        verify(httpResponse).body();
        assertHttpRequest();
    }

    @Test
    void returnsEmptyOptionalOnHttpClientError() throws Exception {
        when(httpClient.send(any(), any())).thenThrow(IOException.class);

        Optional<BigDecimal> result = client.getConversionRate();

        assertThat(result).isNotPresent();
        verify(httpClient).send(requestCaptor.capture(), eq(BodyHandlers.ofString()));
        assertHttpRequest();
    }

    @Test
    @DisplayName("Returns an empty optional when the returned JSON doesn't contain the rate")
    void returnsEmptyOptionalOnInvalidJson() throws Exception {
        when(httpClient.send(any(), any())).thenReturn(httpResponse);
        when(httpResponse.body()).thenReturn("{}");

        Optional<BigDecimal> result = client.getConversionRate();

        assertThat(result).isNotPresent();
        verify(httpClient).send(requestCaptor.capture(), eq(BodyHandlers.ofString()));
        verify(httpResponse).body();
        assertHttpRequest();
    }

    private void assertHttpRequest() {
        HttpRequest request = requestCaptor.getValue();
        assertThat(request.uri()).isEqualTo(EXPECTED_URI);
        assertThat(request.method()).isEqualTo("GET");
    }

    @Nested
    class ConstructorTests {

        @Test
        void throwsOnNullFrom() {
            Throwable thrown = catchThrowable(() -> new CurrConvApiClient(null, TO, API_KEY));

            assertThat(thrown)
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("from");
        }

        @Test
        void throwsOnNullTo() {
            Throwable thrown = catchThrowable(() -> new CurrConvApiClient(FROM, null, API_KEY));

            assertThat(thrown)
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("to");
        }

        @Test
        void throwsOnNullApiKey() {
            Throwable thrown = catchThrowable(() -> new CurrConvApiClient(FROM, TO, null));

            assertThat(thrown)
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("apiKey");
        }
    }
}
