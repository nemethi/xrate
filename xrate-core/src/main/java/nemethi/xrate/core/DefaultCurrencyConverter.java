package nemethi.xrate.core;

import nemethi.xrate.api.ConversionException;
import nemethi.xrate.api.ConversionResult;
import nemethi.xrate.api.CurrencyConverter;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Optional;

public class DefaultCurrencyConverter implements CurrencyConverter {

    private final CurrConvApiClient client;
    private String authCredentials;

    public DefaultCurrencyConverter() {
        this(new CurrConvApiClient());
    }

    DefaultCurrencyConverter(CurrConvApiClient client) {
        this.client = client;
    }

    @Override
    @NotNull
    public ConversionResult convert(@NotNull Currency from, @NotNull Currency to, @NotNull BigDecimal amount) throws ConversionException {
        Optional<BigDecimal> rate = client.getConversionRate(from, to, authCredentials);
        if (rate.isPresent()) {
            BigDecimal result = amount.multiply(rate.get(), ConversionResult.MATH_CONTEXT);
            return new ConversionResult(from, to, amount, result);
        }
        throw new ConversionException("Could not convert currency using default third-party API.");
    }

    @Override
    public void setAuthCredentials(@NotNull String authCredentials) {
        this.authCredentials = authCredentials;
    }
}
