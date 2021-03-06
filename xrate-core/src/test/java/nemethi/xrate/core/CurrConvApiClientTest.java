package nemethi.xrate.core;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
    private static final String ENDPOINT_URI = "http://testUri";
    private static final String URI_TEMPLATE = ENDPOINT_URI + "?q=%s_%s&apiKey=%s&compact=ultra";
    private static final URI EXPECTED_URI = URI.create(String.format(URI_TEMPLATE, FROM, TO, API_KEY));
    private static final String EXCHANGE_RATE_NOT_FOUND_TEMPLATE = "The exchange rate of %s to %s is not found";

    @Mock
    private HttpClient httpClient;
    @Mock
    private HttpResponse httpResponse;
    @Captor
    private ArgumentCaptor<HttpRequest> requestCaptor;

    private CurrConvApiClient client;

    @BeforeEach
    void setUp() {
        client = new CurrConvApiClient(ENDPOINT_URI, httpClient);
    }

    @Test
    void getConversionRateFromThirdParty() throws Exception {
        var key = String.format("%s_%s", FROM, TO);
        var expectedRate = "0.756";
        var responseBody = new JSONObject().put(key, expectedRate).toString();
        when(httpClient.send(any(), any())).thenReturn(httpResponse);
        when(httpResponse.body()).thenReturn(responseBody);

        BigDecimal result = client.getConversionRate(FROM, TO, API_KEY);

        assertThat(result).isEqualTo(new BigDecimal(expectedRate));
        verify(httpClient).send(requestCaptor.capture(), eq(BodyHandlers.ofString()));
        verify(httpResponse).body();
        assertHttpRequest();
    }

    @Test
    void throwsExceptionOnInterruption(@Mock InterruptedException exception) throws IOException, InterruptedException {
        when(httpClient.send(any(), any())).thenThrow(exception);

        Throwable thrown = catchThrowable(() -> client.getConversionRate(FROM, TO, API_KEY));

        assertThat(thrown).isEqualTo(exception);
        verify(httpClient).send(requestCaptor.capture(), eq(BodyHandlers.ofString()));
        assertHttpRequest();
    }

    @Test
    void throwsExceptionOnMalformedEndpointUri() {
        client = new CurrConvApiClient("[malformed");

        Throwable thrown = catchThrowable(() -> client.getConversionRate(FROM, TO, API_KEY));

        assertThat(thrown).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void throwsExceptionOnHttpClientError(@Mock IOException exception) throws Exception {
        when(httpClient.send(any(), any())).thenThrow(exception);

        Throwable thrown = catchThrowable(() -> client.getConversionRate(FROM, TO, API_KEY));

        assertThat(thrown).isEqualTo(exception);
        verify(httpClient).send(requestCaptor.capture(), eq(BodyHandlers.ofString()));
        assertHttpRequest();
    }

    @Test
    @DisplayName("Throws an exception with a formal message when the returned JSON doesn't contain the rate")
    void throwsExceptionWithFormalMessageOnInvalidJson() throws Exception {
        when(httpClient.send(any(), any())).thenReturn(httpResponse);
        when(httpResponse.body()).thenReturn("{}");

        Throwable thrown = catchThrowable(() -> client.getConversionRate(FROM, TO, API_KEY));

        assertThat(thrown)
                .isInstanceOf(JSONException.class)
                .hasMessage(EXCHANGE_RATE_NOT_FOUND_TEMPLATE, FROM, TO);
        verify(httpClient).send(requestCaptor.capture(), eq(BodyHandlers.ofString()));
        verify(httpResponse).body();
        assertHttpRequest();
    }

    @Test
    @DisplayName("Throws an exception with the original message on other JSON-related errors")
    void throwsExceptionOnOtherJsonRelatedError() throws IOException, InterruptedException {
        when(httpClient.send(any(), any())).thenReturn(httpResponse);
        when(httpResponse.body()).thenReturn("");

        Throwable thrown = catchThrowable(() -> client.getConversionRate(FROM, TO, API_KEY));

        assertThat(thrown).isInstanceOf(JSONException.class);
        assertThat(thrown.getMessage()).isNotEqualTo(String.format(EXCHANGE_RATE_NOT_FOUND_TEMPLATE, FROM, TO));
        verify(httpClient).send(requestCaptor.capture(), eq(BodyHandlers.ofString()));
        verify(httpResponse).body();
        assertHttpRequest();
    }

    private void assertHttpRequest() {
        HttpRequest request = requestCaptor.getValue();
        assertThat(request.uri()).isEqualTo(EXPECTED_URI);
        assertThat(request.method()).isEqualTo("GET");
    }
}
