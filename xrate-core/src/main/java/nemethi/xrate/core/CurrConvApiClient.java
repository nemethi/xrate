package nemethi.xrate.core;

import org.json.JSONObject;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.Currency;
import java.util.Optional;

public class CurrConvApiClient {

    private static final String API_ENDPOINT = "https://free.currconv.com/api/v7/convert";
    private static final String QUERY_PARAMS_TEMPLATE = "%s?q=%s_%s&apiKey=%s&compact=ultra";

    private final String endpointUri;
    private final HttpClient httpClient;

    public CurrConvApiClient() {
        this(API_ENDPOINT, HttpClient.newHttpClient());
    }

    public CurrConvApiClient(String endpointUri) {
        this(endpointUri, HttpClient.newHttpClient());
    }

    CurrConvApiClient(String endpointUri, HttpClient httpClient) {
        this.endpointUri = endpointUri;
        this.httpClient = httpClient;
    }

    public Optional<BigDecimal> getConversionRate(Currency from, Currency to, String apiKey) {
        return getConversionRate(from.getCurrencyCode(), to.getCurrencyCode(), apiKey);
    }

    private Optional<BigDecimal> getConversionRate(String fromCurrency, String toCurrency, String apiKey) {
        try {
            URI uri = buildUri(fromCurrency, toCurrency, apiKey);
            HttpRequest request = HttpRequest.newBuilder(uri).build();
            BigDecimal result = sendRequest(request, fromCurrency, toCurrency);
            return Optional.of(result);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private URI buildUri(String from, String to, String apiKey) {
        return URI.create(String.format(QUERY_PARAMS_TEMPLATE, endpointUri, from, to, apiKey));
    }

    private BigDecimal sendRequest(HttpRequest request, String fromCurrency, String toCurrency) throws IOException, InterruptedException {
        HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());
        String result = parseJson(response.body(), fromCurrency, toCurrency);
        return new BigDecimal(result);
    }

    // The getBigDecimal() method is intentionally not used
    // because the BigDecimal(String) constructor is more precise
    private String parseJson(String json, String from, String to) {
        String key = String.format("%s_%s", from, to);
        return new JSONObject(json).getNumber(key).toString();
    }
}
