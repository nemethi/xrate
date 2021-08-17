package nemethi.xrate.core;

import nemethi.xrate.api.ConversionException;
import nemethi.xrate.api.ConversionResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultCurrencyConverterTest {

    private static final String API_KEY = "testApiKey";
    private static final Currency FROM = Currency.getInstance("USD");
    private static final Currency TO = Currency.getInstance("GBP");
    private static final BigDecimal AMOUNT = BigDecimal.ONE;
    private static final BigDecimal RATE = new BigDecimal("1.5");
    private static final BigDecimal RESULT = AMOUNT.multiply(RATE, ConversionResult.MATH_CONTEXT);
    private static final String ERROR_MESSAGE = "Could not convert currency using default third-party API.";

    @Mock
    private CurrConvApiClient client;

    private DefaultCurrencyConverter converter;

    @BeforeEach
    void setUp() {
        converter = new DefaultCurrencyConverter(client);
        converter.setAuthCredentials(API_KEY);
    }

    @Test
    void multipliesAmountWithRateReturnedFromClient() {
        when(client.getConversionRate(any(), any(), anyString())).thenReturn(Optional.of(RATE));
        ConversionResult expectedResult = new ConversionResult(FROM, TO, AMOUNT, RESULT);

        ConversionResult result = converter.convert(FROM, TO, AMOUNT);

        assertThat(result).isEqualTo(expectedResult);
        verify(client).getConversionRate(FROM, TO, API_KEY);
    }

    @Test
    void throwsExceptionWhenRateIsNotPresent() {
        when(client.getConversionRate(any(), any(), anyString())).thenReturn(Optional.empty());

        Throwable thrown = catchThrowable(() -> converter.convert(FROM, TO, AMOUNT));

        assertThat(thrown)
                .isInstanceOf(ConversionException.class)
                .hasMessage(ERROR_MESSAGE);
        verify(client).getConversionRate(FROM, TO, API_KEY);
    }
}
