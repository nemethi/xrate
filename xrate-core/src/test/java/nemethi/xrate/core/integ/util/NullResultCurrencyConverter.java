package nemethi.xrate.core.integ.util;

import nemethi.xrate.api.ConversionException;
import nemethi.xrate.api.ConversionResult;
import nemethi.xrate.api.CurrencyConverter;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.Currency;

public class NullResultCurrencyConverter implements CurrencyConverter {

    @Override
    @NotNull
    public ConversionResult convert(@NotNull Currency from, @NotNull Currency to, @NotNull BigDecimal amount) throws ConversionException {
        return null;
    }

    @Override
    public void setAuthCredentials(@NotNull String authCredentials) {

    }
}
