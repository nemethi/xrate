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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
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
    private static final String PLUGIN_AUTH_CREDS = "testPluginAuthCreds";
    private static final Exception EXPECTED_EXCEPTION = new ConversionException("testMessage");
    private static final String MISSING_RESULT_ERROR_MESSAGE = "Error: converter did not return any result";

    @Mock
    private Configuration config;
    @Mock
    private PluginLoader loader;
    @Mock
    private ResultPrinter printer;
    @Mock
    private CurrencyConverter converter;

    private Xrate xrate;

    @BeforeEach
    void setUp() {
        xrate = Mockito.spy(new Xrate(config, loader, printer));
    }

    @Test
    void convertWithPlugin() {
        when(loader.findFirstPlugin()).thenReturn(Optional.of(converter));
        when(config.getPluginAuthCredentials()).thenReturn(PLUGIN_AUTH_CREDS);
        when(converter.convert(any(), any(), any())).thenReturn(CONVERSION_RESULT);

        xrate.convert(FROM, TO, AMOUNT);

        verify(loader).findFirstPlugin();
        verify(config).getPluginAuthCredentials();
        verify(converter).setAuthCredentials(PLUGIN_AUTH_CREDS);
        verify(converter).convert(FROM, TO, AMOUNT);
        verify(printer).print(CONVERSION_RESULT);
    }

    @Test
    void doesNotCatchExceptionOfPlugin() {
        when(loader.findFirstPlugin()).thenReturn(Optional.of(converter));
        when(config.getPluginAuthCredentials()).thenReturn(PLUGIN_AUTH_CREDS);
        when(converter.convert(any(), any(), any())).thenThrow(EXPECTED_EXCEPTION);

        Throwable thrown = catchThrowable(() -> xrate.convert(FROM, TO, AMOUNT));

        assertThat(thrown).isEqualTo(EXPECTED_EXCEPTION);
        verify(loader).findFirstPlugin();
        verify(config).getPluginAuthCredentials();
        verify(converter).setAuthCredentials(PLUGIN_AUTH_CREDS);
        verify(converter).convert(FROM, TO, AMOUNT);
    }

    @Test
    void convertWithDefaultConverter() {
        when(loader.findFirstPlugin()).thenReturn(Optional.empty());
        when(config.getCurrConvEndpoint()).thenReturn(ENDPOINT);
        when(config.getCoreAuthCredentials()).thenReturn(AUTH_CREDS);
        doReturn(converter).when(xrate).createDefaultConverter(any(), anyString());
        when(converter.convert(any(), any(), any())).thenReturn(CONVERSION_RESULT);

        xrate.convert(FROM, TO, AMOUNT);

        verify(loader).findFirstPlugin();
        verify(config).getCurrConvEndpoint();
        verify(config).getCoreAuthCredentials();
        verify(xrate).createDefaultConverter(any(), eq(AUTH_CREDS));
        verify(converter).convert(FROM, TO, AMOUNT);
        verify(printer).print(CONVERSION_RESULT);
    }

    @Test
    void doesNotCatchExceptionOfDefaultConverter() {
        when(loader.findFirstPlugin()).thenReturn(Optional.empty());
        when(config.getCurrConvEndpoint()).thenReturn(ENDPOINT);
        when(config.getCoreAuthCredentials()).thenReturn(AUTH_CREDS);
        doReturn(converter).when(xrate).createDefaultConverter(any(), anyString());
        when(converter.convert(any(), any(), any())).thenThrow(EXPECTED_EXCEPTION);

        Throwable thrown = catchThrowable(() -> xrate.convert(FROM, TO, AMOUNT));

        assertThat(thrown).isEqualTo(EXPECTED_EXCEPTION);
        verify(loader).findFirstPlugin();
        verify(config).getCurrConvEndpoint();
        verify(config).getCoreAuthCredentials();
        verify(xrate).createDefaultConverter(any(), eq(AUTH_CREDS));
        verify(converter).convert(FROM, TO, AMOUNT);
    }

    @Test
    void throwsExceptionOnNullResult() {
        when(loader.findFirstPlugin()).thenReturn(Optional.of(converter));
        when(config.getPluginAuthCredentials()).thenReturn(PLUGIN_AUTH_CREDS);
        when(converter.convert(any(), any(), any())).thenReturn(null);

        Throwable thrown = catchThrowable(() -> xrate.convert(FROM, TO, AMOUNT));

        assertThat(thrown)
                .isInstanceOf(ConversionException.class)
                .hasMessage(MISSING_RESULT_ERROR_MESSAGE);
        verify(loader).findFirstPlugin();
        verify(config).getPluginAuthCredentials();
        verify(converter).setAuthCredentials(PLUGIN_AUTH_CREDS);
        verify(converter).convert(FROM, TO, AMOUNT);
        verify(printer, never()).print(any());
    }
}
