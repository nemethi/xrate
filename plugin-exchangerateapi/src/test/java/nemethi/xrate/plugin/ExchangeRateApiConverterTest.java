package nemethi.xrate.plugin;

import nemethi.xrate.api.ConversionException;
import nemethi.xrate.api.ConversionResult;
import org.apache.juneau.rest.client2.RestCallException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExchangeRateApiConverterTest {

    private static final Currency FROM = Currency.getInstance("EUR");
    private static final Currency TO = Currency.getInstance("HUF");
    private static final BigDecimal AMOUNT = new BigDecimal("2");
    private static final String API_KEY = "testApiKey";
    private static final String EXCEPTION_MESSAGE = "testExceptionMessage";

    @Mock
    private ExchangeRateApiClient client;

    private ExchangeRateApiConverter converter;

    @BeforeEach
    void setUp() {
        converter = new ExchangeRateApiConverter(client);
        converter.setAuthCredentials(API_KEY);
    }

    @Test
    void convert() throws RestCallException, ExchangeRateApiException, URISyntaxException {
        final var expectedResult = new BigDecimal("42");
        final var expectedConversionResult = new ConversionResult(FROM, TO, AMOUNT, expectedResult);
        when(client.convert(any(), any(), any(), any())).thenReturn(expectedResult);

        ConversionResult result = converter.convert(FROM, TO, AMOUNT);

        assertThat(result).isEqualTo(expectedConversionResult);
        verify(client).convert(FROM, TO, AMOUNT, API_KEY);
    }

    @Test
    void wrapsUriSyntaxException(@Mock URISyntaxException exception) throws RestCallException, ExchangeRateApiException, URISyntaxException {
        when(client.convert(any(), any(), any(), any())).thenThrow(exception);
        when(exception.getMessage()).thenReturn(EXCEPTION_MESSAGE);

        Throwable thrown = catchThrowable(() -> converter.convert(FROM, TO, AMOUNT));

        assertThat(thrown)
                .isInstanceOf(ConversionException.class)
                .hasMessage("Error while building URI for ExchangeRate-API: %s", EXCEPTION_MESSAGE);
        verify(client).convert(FROM, TO, AMOUNT, API_KEY);
        verify(exception).getMessage();
    }

    @Test
    void wrapsExceptionOfOtherKind(@Mock ExchangeRateApiException exception) throws RestCallException, ExchangeRateApiException, URISyntaxException {
        when(client.convert(any(), any(), any(), any())).thenThrow(exception);
        when(exception.getMessage()).thenReturn(EXCEPTION_MESSAGE);

        Throwable thrown = catchThrowable(() -> converter.convert(FROM, TO, AMOUNT));

        assertThat(thrown)
                .isInstanceOf(ConversionException.class)
                .hasMessage("Error while calling ExchangeRate-API: %s", EXCEPTION_MESSAGE);
        verify(client).convert(FROM, TO, AMOUNT, API_KEY);
        verify(exception).getMessage();
    }
}
