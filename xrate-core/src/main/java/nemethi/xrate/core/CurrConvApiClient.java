package nemethi.xrate.core;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.Currency;

public class CurrConvApiClient {

    private static final String QUERY_PARAMS_TEMPLATE = "%s?q=%s_%s&apiKey=%s&compact=ultra";
    private static final String KEY_NOT_FOUND_TEMPLATE = "JSONObject[\"%s\"] not found.";
    private static final String EXCHANGE_RATE_NOT_FOUND_TEMPLATE = "The exchange rate of %s to %s is not found";

    private final String endpointUri;
    private final HttpClient httpClient;

    public CurrConvApiClient(String endpointUri) {
        this(endpointUri, HttpClient.newHttpClient());
    }

    CurrConvApiClient(String endpointUri, HttpClient httpClient) {
        this.endpointUri = endpointUri;
        this.httpClient = httpClient;
    }

    public BigDecimal getConversionRate(Currency from, Currency to, String apiKey) throws IOException, InterruptedException {
        return getConversionRate(from.getCurrencyCode(), to.getCurrencyCode(), apiKey);
    }

    private BigDecimal getConversionRate(String fromCurrency, String toCurrency, String apiKey) throws IOException, InterruptedException {
        URI uri = buildUri(fromCurrency, toCurrency, apiKey);
        HttpRequest request = HttpRequest.newBuilder(uri).build();
        return sendRequest(request, fromCurrency, toCurrency);
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
        return getRateFromJson(json, from, to, key);
    }

    private String getRateFromJson(String json, String from, String to, String key) {
        try {
            return new JSONObject(json).getNumber(key).toString();
        } catch (JSONException e) {
            if (keyIsNotFound(e, key)) {
                throw exceptionWithMoreFormalMessage(e.getCause(), from, to);
            }
            throw e;
        }
    }

    private boolean keyIsNotFound(JSONException exception, String key) {
        String message = String.format(KEY_NOT_FOUND_TEMPLATE, key);
        return message.equals(exception.getMessage());
    }

    private JSONException exceptionWithMoreFormalMessage(Throwable cause, String from, String to) {
        return new JSONException(String.format(EXCHANGE_RATE_NOT_FOUND_TEMPLATE, from, to), cause);
    }
}
