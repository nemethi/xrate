package nemethi.xrate.core;

import nemethi.xrate.api.ConversionException;
import nemethi.xrate.api.ConversionResult;
import nemethi.xrate.api.CurrencyConverter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class XrateTest {

    private static final Currency FROM = Currency.getInstance("USD");
    private static final Currency TO = Currency.getInstance("GBP");
    private static final BigDecimal AMOUNT = BigDecimal.ONE;
    private static final BigDecimal RESULT = new BigDecimal("0.72");
    private static final ConversionResult CONVERSION_RESULT = new ConversionResult(FROM, TO, AMOUNT, RESULT);
    private static final String ENDPOINT = "testEndpoint";
    private static final String AUTH_CREDS = "testAuthCreds";
    private static final Exception EXPECTED_EXCEPTION = new ConversionException("testMessage");

    @Mock
    private Configuration config;
    @Mock
    private ResultPrinter printer;
    @Mock
    private CurrencyConverter converter;

    private Xrate xrate;

    @BeforeEach
    void setUp() {
        xrate = Mockito.spy(new Xrate(config, printer));
    }

    @Test
    void convert() {
        when(config.getCurrConvEndpoint()).thenReturn(ENDPOINT);
        when(config.getCoreAuthCredentials()).thenReturn(AUTH_CREDS);
        doReturn(converter).when(xrate).createDefaultConverter(any(), anyString());
        when(converter.convert(any(), any(), any())).thenReturn(CONVERSION_RESULT);

        xrate.convert(FROM, TO, AMOUNT);

        verify(config).getCurrConvEndpoint();
        verify(config).getCoreAuthCredentials();
        verify(xrate).createDefaultConverter(any(), eq(AUTH_CREDS));
        verify(converter).convert(FROM, TO, AMOUNT);
        verify(printer).print(CONVERSION_RESULT);
    }

    @Test
    void convertDoesNotCatchException() {
        when(config.getCurrConvEndpoint()).thenReturn(ENDPOINT);
        when(config.getCoreAuthCredentials()).thenReturn(AUTH_CREDS);
        doReturn(converter).when(xrate).createDefaultConverter(any(), anyString());
        when(converter.convert(any(), any(), any())).thenThrow(EXPECTED_EXCEPTION);

        Throwable thrown = catchThrowable(() -> xrate.convert(FROM, TO, AMOUNT));

        assertThat(thrown).isEqualTo(EXPECTED_EXCEPTION);
        verify(config).getCurrConvEndpoint();
        verify(config).getCoreAuthCredentials();
        verify(xrate).createDefaultConverter(any(), eq(AUTH_CREDS));
        verify(converter).convert(FROM, TO, AMOUNT);
    }
}
