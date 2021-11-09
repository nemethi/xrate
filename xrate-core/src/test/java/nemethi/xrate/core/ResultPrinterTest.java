package nemethi.xrate.core;

import nemethi.xrate.api.ConversionResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Currency;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ResultPrinterTest {

    private static final Currency FROM = Currency.getInstance("EUR");
    private static final Currency TO = Currency.getInstance("HUF");
    private static final BigDecimal AMOUNT = new BigDecimal("768.13");
    private static final BigDecimal RESULT = new BigDecimal("278275.41");
    private static final ConversionResult CONVERSION_RESULT = new ConversionResult(FROM, TO, AMOUNT, RESULT);
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#,###.####");
    private static final String RESULT_FORMAT = "%s %s = %s %s";
    private static final String RATE_FORMAT = "1 %s = %s %s";

    @Mock
    private PrintWriter writer;

    private ResultPrinter printer;

    @BeforeEach
    void setUp() {
        printer = new ResultPrinter(writer);
    }

    @Test
    void print() {
        printer.print(CONVERSION_RESULT);

        verify(writer).println(formattedResult());
        verify(writer).println(formattedRate());
        verify(writer).println(formattedInverseRate());
    }

    private String formattedResult() {
        return String.format(RESULT_FORMAT, format(AMOUNT), FROM, format(RESULT), TO);
    }

    private String formattedRate() {
        return String.format(RATE_FORMAT, FROM, format(CONVERSION_RESULT.getRate()), TO);
    }

    private String formattedInverseRate() {
        return String.format(RATE_FORMAT, TO, format(CONVERSION_RESULT.getInverseRate()), FROM);
    }

    private String format(BigDecimal number) {
        return DECIMAL_FORMAT.format(number);
    }
}
