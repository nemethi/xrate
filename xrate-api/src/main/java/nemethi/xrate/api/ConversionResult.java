package nemethi.xrate.api;

import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.Objects;

import static java.util.Objects.requireNonNull;

/**
 * Represents the immutable result of a currency conversion.
 * Apart from the resulting amount it contains the initial parameters of the conversion.
 * It also provides methods for getting the rate and the inverse rate.
 */
public class ConversionResult {

    private static final int PRECISION = 3;
    /**
     * Contains information about precision and rounding mode for numerical operations.
     */
    public static final MathContext MATH_CONTEXT = new MathContext(PRECISION, RoundingMode.HALF_EVEN);

    private final Currency from;
    private final Currency to;
    private final BigDecimal amount;
    private final BigDecimal result;

    /**
     * Creates a new {@code ConversionResult} instance.
     * <p>
     * If any of the parameters is null, a {@link NullPointerException} is thrown.
     *
     * @param from   the currency converted from
     * @param to     the currency converted to
     * @param amount the amount converted
     * @param result the result of the conversion
     */
    public ConversionResult(@NotNull Currency from, @NotNull Currency to,
                            @NotNull BigDecimal amount, @NotNull BigDecimal result) {
        this.from = requireNonNull(from, "Parameter 'from' cannot be null");
        this.to = requireNonNull(to, "Parameter 'to' cannot be null");
        this.amount = requireNonNull(amount, "Parameter 'amount' cannot be null");
        this.result = requireNonNull(result, "Parameter 'result' cannot be null");
    }

    /**
     * Returns the exchange rate used for the conversion.
     * For example, if 2 USD = 1.44 GBP then the rate is 0.72.
     * <p>
     * Thus 1 USD = 0.72 GBP.
     *
     * @return the quotient of <code>result</code> divided by <code>amount</code>
     */
    public BigDecimal getRate() {
        return result.divide(amount, MATH_CONTEXT);
    }

    /**
     * Returns the inverse of the exchange rate used for the conversion.
     * For example, if 2 USD = 1.44 GBP then the inverse rate is 1.38.
     * <p>
     * Thus 1 GBP = 1.38 USD.
     *
     * @return the quotient of <code>amount</code> divided by <code>result</code>
     */
    public BigDecimal getInverseRate() {
        return amount.divide(result, MATH_CONTEXT);
    }

    /**
     * Returns the currency the given amount is converted from.
     *
     * @return the currency converted from
     */
    public Currency getFrom() {
        return from;
    }

    /**
     * Returns the currency the given amount is converted to.
     *
     * @return the currency converted to
     */
    public Currency getTo() {
        return to;
    }

    /**
     * Returns the conversion amount.
     *
     * @return the amount converted
     */
    public BigDecimal getAmount() {
        return amount;
    }

    /**
     * Returns the conversion result.
     *
     * @return the result of the conversion
     */
    public BigDecimal getResult() {
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ConversionResult that = (ConversionResult) o;
        return from.equals(that.from) &&
                to.equals(that.to) &&
                amount.equals(that.amount) &&
                result.equals(that.result);
    }

    @Override
    public int hashCode() {
        return Objects.hash(from, to, amount, result);
    }

    @Override
    public String toString() {
        return "ConversionResult{" +
                "from=" + from +
                ", to=" + to +
                ", amount=" + amount +
                ", result=" + result +
                '}';
    }
}
