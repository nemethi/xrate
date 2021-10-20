package nemethi.xrate.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

class ConversionResultTest {

    private static final Currency FROM = Currency.getInstance("USD");
    private static final Currency TO = Currency.getInstance("GBP");
    private static final BigDecimal AMOUNT = BigDecimal.ONE;
    private static final BigDecimal RESULT = new BigDecimal("0.72");
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_EVEN;
    private static final int NUMBER_OF_DECIMAL_PLACES = 2;

    private ConversionResult conversionResult;

    @BeforeEach
    void setUp() {
        conversionResult = new ConversionResult(FROM, TO, AMOUNT, RESULT);
    }

    @Test
    void gettersWork() {
        assertThat(conversionResult.getFrom()).isEqualTo(FROM);
        assertThat(conversionResult.getTo()).isEqualTo(TO);
        assertThat(conversionResult.getAmount()).isEqualTo(AMOUNT);
        assertThat(conversionResult.getResult()).isEqualTo(RESULT);
    }

    @Test
    @DisplayName("getRate() returns the quotient of result divided by amount")
    void getRateWorks() {
        BigDecimal expectedRate = RESULT.divide(AMOUNT, NUMBER_OF_DECIMAL_PLACES, ROUNDING_MODE);
        BigDecimal rate = conversionResult.getRate();
        assertThat(rate).isEqualByComparingTo(expectedRate);
    }

    @Test
    void getRateWithZeroAmount() {
        var conversionResult = new ConversionResult(FROM, TO, BigDecimal.ZERO, RESULT);
        Throwable thrown = catchThrowable(conversionResult::getRate);
        assertThat(thrown).isInstanceOf(ArithmeticException.class);
    }

    @Test
    @DisplayName("getInverseRate() returns the quotient of amount divided by result")
    void getInverseRateWorks() {
        BigDecimal expectedInverseRate = AMOUNT.divide(RESULT, NUMBER_OF_DECIMAL_PLACES, ROUNDING_MODE);
        BigDecimal inverseRate = conversionResult.getInverseRate();
        assertThat(inverseRate).isEqualByComparingTo(expectedInverseRate);
    }

    @Test
    void getInverseRateWithZeroResult() {
        var conversionResult = new ConversionResult(FROM, TO, AMOUNT, BigDecimal.ZERO);
        Throwable thrown = catchThrowable(conversionResult::getInverseRate);
        assertThat(thrown).isInstanceOf(ArithmeticException.class);
    }

    @Nested
    class ConstructorTests {

        @Test
        void throwsOnNullFrom() {
            Throwable thrown = catchThrowable(() -> new ConversionResult(null, TO, AMOUNT, RESULT));

            assertThat(thrown)
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("Parameter 'from' cannot be null");
        }

        @Test
        void throwsOnNullTo() {
            Throwable thrown = catchThrowable(() -> new ConversionResult(FROM, null, AMOUNT, RESULT));

            assertThat(thrown)
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("Parameter 'to' cannot be null");
        }

        @Test
        void throwsOnNullAmount() {
            Throwable thrown = catchThrowable(() -> new ConversionResult(FROM, TO, null, RESULT));

            assertThat(thrown)
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("Parameter 'amount' cannot be null");
        }

        @Test
        void throwsOnNullResult() {
            Throwable thrown = catchThrowable(() -> new ConversionResult(FROM, TO, AMOUNT, null));

            assertThat(thrown)
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("Parameter 'result' cannot be null");
        }
    }

    @Nested
    @DisplayName("Tests of Object's overridden methods")
    class ObjectMethodsTests {

        @Test
        void equalsIsReflexive() {
            assertThat(conversionResult.equals(conversionResult)).isTrue();
        }

        @Test
        void equalsIsSymmetric() {
            var otherResult = new ConversionResult(FROM, TO, AMOUNT, RESULT);
            assertThat(conversionResult.equals(otherResult)).isTrue();
            assertThat(otherResult.equals(conversionResult)).isTrue();
        }

        @Test
        void equalsReturnsFalseOnNull() {
            assertThat(conversionResult.equals(null)).isFalse();
        }

        @Test
        void equalsReturnsFalseOnDifferentClass() {
            assertThat(conversionResult.equals(new Object())).isFalse();
        }

        @Test
        void equalsReturnsFalseOnDifferentFrom() {
            var differentResult = new ConversionResult(Currency.getInstance("CAD"), TO, AMOUNT, RESULT);
            assertThat(conversionResult.equals(differentResult)).isFalse();
        }

        @Test
        void equalsReturnsFalseOnDifferentTo() {
            var differentResult = new ConversionResult(FROM, Currency.getInstance("CAD"), AMOUNT, RESULT);
            assertThat(conversionResult.equals(differentResult)).isFalse();
        }

        @Test
        void equalsReturnsFalseOnDifferentAmount() {
            var differentResult = new ConversionResult(FROM, TO, BigDecimal.ZERO, RESULT);
            assertThat(conversionResult.equals(differentResult)).isFalse();
        }

        @Test
        void equalsReturnsFalseOnDifferentResult() {
            var differentResult = new ConversionResult(FROM, TO, AMOUNT, BigDecimal.ZERO);
            assertThat(conversionResult.equals(differentResult)).isFalse();
        }

        @Test
        void equalObjectsHaveSameHashCode() {
            var otherResult = new ConversionResult(FROM, TO, AMOUNT, RESULT);
            assertThat(conversionResult.hashCode()).isEqualTo(otherResult.hashCode());
        }

        @Test
        void toStringContainsTheFieldNamesAndValues() {
            var string = conversionResult.toString();
            assertThat(string).contains("from", FROM.toString(), "to", TO.toString(),
                    "amount", AMOUNT.toString(), "result", RESULT.toString());
        }
    }
}
