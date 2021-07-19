package nemethi.xrate.api;

import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.Currency;

/**
 * Converts an amount of money from a currency to another currency, perhaps using a third-party service.
 */
public interface CurrencyConverter {

    /**
     * Converts the specified amount from a currency to another currency.
     * Returns the result of the conversion as a {@link ConversionResult}.
     * The returned value must never be null.
     * <p>
     * This method is free to throw an unchecked exception if any error occurs.
     *
     * @param from   the currency converted from
     * @param to     the currency converted to
     * @param amount the amount converted
     * @return the result of the conversion
     */
    @NotNull
    ConversionResult convert(@NotNull Currency from, @NotNull Currency to, @NotNull BigDecimal amount);

    /**
     * Sets the credentials used for authentication, if there is any.
     *
     * @param authCredentials the API key, username + password, etc. used for authenticating with a third-party service
     */
    void setAuthCredentials(@NotNull String authCredentials);
}
