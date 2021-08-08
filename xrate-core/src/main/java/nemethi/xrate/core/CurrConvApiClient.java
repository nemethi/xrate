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

import static java.util.Objects.requireNonNull;

public class CurrConvApiClient {

    private static final String API_ENDPOINT = "https://free.currconv.com/api/v7/convert";
    private static final String QUERY_PARAMS_TEMPLATE = "%s?q=%s_%s&apiKey=%s&compact=ultra";

    private final String from;
    private final String to;
    private final String apiKey;
    private String endpointUri;
    private HttpClient httpClient;

    public CurrConvApiClient(Currency from, Currency to, String apiKey) {
        this.from = requireNonNull(from, "from").getCurrencyCode();
        this.to = requireNonNull(to, "to").getCurrencyCode();
        this.apiKey = requireNonNull(apiKey, "apiKey");
        this.endpointUri = API_ENDPOINT;
        this.httpClient = HttpClient.newHttpClient();
    }

    public Optional<BigDecimal> getConversionRate() {
        try {
            HttpRequest request = HttpRequest.newBuilder(buildUri()).build();
            BigDecimal result = sendRequest(request);
            return Optional.of(result);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private URI buildUri() {
        return URI.create(String.format(QUERY_PARAMS_TEMPLATE, endpointUri, from, to, apiKey));
    }

    private BigDecimal sendRequest(HttpRequest request) throws IOException, InterruptedException {
        HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());
        String result = parseJson(response.body());
        return new BigDecimal(result);
    }

    // The getBigDecimal() method is intentionally not used
    // because the BigDecimal(String) constructor is more precise
    private String parseJson(String json) {
        String key = String.format("%s_%s", from, to);
        return new JSONObject(json).getNumber(key).toString();
    }

    public void setEndpointUri(String endpointUri) {
        this.endpointUri = endpointUri;
    }

    public void setHttpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }
}
