package nemethi.xrate.core.integ.util;

import nemethi.xrate.api.ConversionException;
import nemethi.xrate.api.ConversionResult;
import nemethi.xrate.api.CurrencyConverter;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.Currency;

public class TestCurrencyConverter implements CurrencyConverter {

    private static String authCredentials;

    @Override
    @NotNull
    public ConversionResult convert(@NotNull Currency from, @NotNull Currency to, @NotNull BigDecimal amount) throws ConversionException {
        BigDecimal result = amount.multiply(new BigDecimal("2"), ConversionResult.MATH_CONTEXT);
        return new ConversionResult(from, to, amount, result);
    }

    @Override
    public void setAuthCredentials(@NotNull String authCredentials) {
        TestCurrencyConverter.authCredentials = authCredentials;
    }

    public static String getAuthCredentials() {
        return authCredentials;
    }
}
