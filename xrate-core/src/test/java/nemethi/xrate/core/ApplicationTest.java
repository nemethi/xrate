package nemethi.xrate.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import picocli.CommandLine;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.ParameterException;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.matches;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApplicationTest {

    private static final Pattern AVAILABLE_CURRENCIES_PATTERN = Pattern.compile("^\\w{3}(, \\w{3})*$");
    private static final String CONFIG_FILE_PATH = "xrate.properties";
    private static final String FROM = "USD";
    private static final String TO = "GBP";
    private static final String AMOUNT = "1";

    @Mock
    private CommandSpec spec;
    @Mock
    private CommandLine commandLine;
    @Mock
    private PrintWriter outWriter;
    @Mock
    private Xrate xrate;
    @Mock(stubOnly = true)
    private Configuration config;

    @Spy
    private Application application;

    @BeforeEach
    void setUp() {
        application.setSpec(spec);
    }

    @Test
    void printAvailableCurrenciesIfSpecified() {
        when(spec.commandLine()).thenReturn(commandLine);
        when(commandLine.getOut()).thenReturn(outWriter);
        application.setListAvailableCurrencies(true);

        Integer exitCode = application.call();

        assertThat(exitCode).isZero();
        verify(spec).commandLine();
        verify(commandLine).getOut();
        verify(outWriter).println(matches(AVAILABLE_CURRENCIES_PATTERN));
    }

    @Test
    void callExecutesMainApplicationLogic() {
        when(spec.commandLine()).thenReturn(commandLine);
        when(commandLine.getOut()).thenReturn(outWriter);
        doReturn(config).when(application).getConfig(anyString());
        doReturn(xrate).when(application).createXrate(any(), any());
        application.setConfigFilePath(CONFIG_FILE_PATH);
        application.setFrom(FROM);
        application.setTo(TO);
        application.setAmount(AMOUNT);

        Integer exitCode = application.call();

        assertThat(exitCode).isZero();
        verify(spec).commandLine();
        verify(commandLine).getOut();
        verify(application).getConfig(CONFIG_FILE_PATH);
        verify(application).createXrate(config, outWriter);
        verify(xrate).convert(Currency.getInstance(FROM), Currency.getInstance(TO), new BigDecimal(AMOUNT));
    }

    @Nested
    class ParameterTests {

        private static final String UNKNOWN_CURRENCY = "AAA";
        private static final String UNKNOWN_CURRENCY_MESSAGE = "Unknown currency: %s";
        private static final String INVALID_AMOUNT_MESSAGE = "Invalid amount: %s";
        private static final String INVALID_AMOUNT = "invalid amount";

        @Test
        void setFromThrowsOnUnknownCurrency() {
            when(spec.commandLine()).thenReturn(commandLine);

            Throwable thrown = catchThrowable(() -> application.setFrom(UNKNOWN_CURRENCY));

            assertThat(thrown).isInstanceOf(ParameterException.class);
            ParameterException exception = (ParameterException) thrown;
            assertThat(exception.getCommandLine()).isEqualTo(commandLine);
            assertThat(exception).hasMessage(UNKNOWN_CURRENCY_MESSAGE, UNKNOWN_CURRENCY);
            verify(spec).commandLine();
        }

        @Test
        void setToThrowsOnUnknownCurrency() {
            when(spec.commandLine()).thenReturn(commandLine);

            Throwable thrown = catchThrowable(() -> application.setTo(UNKNOWN_CURRENCY));

            assertThat(thrown).isInstanceOf(ParameterException.class);
            ParameterException exception = (ParameterException) thrown;
            assertThat(exception.getCommandLine()).isEqualTo(commandLine);
            assertThat(exception).hasMessage(UNKNOWN_CURRENCY_MESSAGE, UNKNOWN_CURRENCY);
            verify(spec).commandLine();
        }

        @Test
        void setAmountThrowsOnInvalidAmount() {
            when(spec.commandLine()).thenReturn(commandLine);

            Throwable thrown = catchThrowable(() -> application.setAmount(INVALID_AMOUNT));

            assertThat(thrown).isInstanceOf(ParameterException.class);
            ParameterException exception = (ParameterException) thrown;
            assertThat(exception.getCommandLine()).isEqualTo(commandLine);
            assertThat(exception).hasMessage(INVALID_AMOUNT_MESSAGE, INVALID_AMOUNT);
            verify(spec).commandLine();
        }
    }
}
