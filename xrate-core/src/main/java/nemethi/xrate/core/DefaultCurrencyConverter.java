package nemethi.xrate.core;

import nemethi.xrate.api.ConversionException;
import nemethi.xrate.api.ConversionResult;
import nemethi.xrate.api.CurrencyConverter;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.Currency;

public class DefaultCurrencyConverter implements CurrencyConverter {

    private static final String ERROR_MESSAGE = "Could not convert currency using default third-party API:";
    private final CurrConvApiClient client;
    private String authCredentials;

    public DefaultCurrencyConverter(CurrConvApiClient client) {
        this.client = client;
    }

    @Override
    @NotNull
    public ConversionResult convert(@NotNull Currency from, @NotNull Currency to, @NotNull BigDecimal amount) throws ConversionException {
        BigDecimal result = amount.multiply(getRate(from, to), ConversionResult.MATH_CONTEXT);
        return new ConversionResult(from, to, amount, result);
    }

    private BigDecimal getRate(@NotNull Currency from, @NotNull Currency to) {
        try {
            return client.getConversionRate(from, to, authCredentials);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ConversionException(ERROR_MESSAGE, e);
        } catch (Exception e) {
            throw new ConversionException(ERROR_MESSAGE, e);
        }
    }

    @Override
    public void setAuthCredentials(@NotNull String authCredentials) {
        this.authCredentials = authCredentials;
    }
}
