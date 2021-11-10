package nemethi.xrate.plugin;

import nemethi.xrate.api.ConversionException;
import nemethi.xrate.api.ConversionResult;
import nemethi.xrate.api.CurrencyConverter;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.util.Currency;

public class ExchangeRateApiConverter implements CurrencyConverter {

    private final ExchangeRateApiClient client;
    private String apiKey;

    public ExchangeRateApiConverter() {
        this(new ExchangeRateApiClient());
    }

    ExchangeRateApiConverter(ExchangeRateApiClient client) {
        this.client = client;
    }

    @Override
    @NotNull
    public ConversionResult convert(@NotNull Currency from, @NotNull Currency to, @NotNull BigDecimal amount) throws ConversionException {
        try {
            BigDecimal result = client.convert(from, to, amount, apiKey);
            return new ConversionResult(from, to, amount, result);
        } catch (URISyntaxException e) {
            throw new ConversionException("Error while building URI for ExchangeRate-API: " + e.getMessage());
        } catch (Exception e) {
            throw new ConversionException("Error while calling ExchangeRate-API: " + e.getMessage());
        }
    }

    @Override
    public void setAuthCredentials(@NotNull String authCredentials) {
        apiKey = authCredentials;
    }
}
