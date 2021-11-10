package nemethi.xrate.plugin;

import org.apache.http.client.utils.URIBuilder;
import org.apache.juneau.rest.client2.RestCallException;
import org.apache.juneau.rest.client2.RestClient;
import org.apache.juneau.rest.client2.RestResponse;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Currency;
import java.util.Map;

import static java.util.Objects.isNull;

public class ExchangeRateApiClient {

    private static final String DEFAULT_ENDPOINT = "https://v6.exchangerate-api.com";
    private static final RestClient DEFAULT_REST_CLIENT = RestClient.create().json().ignoreErrors().build();

    private final String endpointUri;
    private final RestClient client;

    public ExchangeRateApiClient() {
        this(DEFAULT_ENDPOINT, DEFAULT_REST_CLIENT);
    }

    public ExchangeRateApiClient(String endpointUri) {
        this(endpointUri, DEFAULT_REST_CLIENT);
    }

    ExchangeRateApiClient(String endpointUri, RestClient client) {
        this.endpointUri = endpointUri;
        this.client = client;
    }

    public BigDecimal convert(Currency from, Currency to, BigDecimal amount, String apiKey) throws URISyntaxException, RestCallException, ExchangeRateApiException {
        URI uri = buildUri(from, to, amount, apiKey);
        RestResponse response = client.get(uri).run();
        return getConversionResult(response);
    }

    private URI buildUri(Currency from, Currency to, BigDecimal amount, String apiKey) throws URISyntaxException {
        URIBuilder uriBuilder = new URIBuilder(endpointUri);
        uriBuilder.setPath(String.format("/v6/%s/pair/%s/%s/%s", apiKey, from, to, amount));
        return uriBuilder.build();
    }

    private BigDecimal getConversionResult(RestResponse response) throws RestCallException, ExchangeRateApiException {
        Map<String, String> body = response.getBody().as(Map.class, String.class, String.class);
        if (isSuccessfulResult(body)) {
            return new BigDecimal(body.get("conversion_result"));
        }
        throw new ExchangeRateApiException(getErrorType(body));
    }

    private boolean isSuccessfulResult(Map<String, String> body) {
        if (isNull(body)) {
            return false;
        }
        return "success".equals(body.get("result"));
    }

    private String getErrorType(Map<String, String> body) {
        if(isNull(body)) {
            return null;
        }
        return body.get("error-type");
    }
}
