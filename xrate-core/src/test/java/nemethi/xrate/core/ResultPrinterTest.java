package nemethi.xrate.core;

import nemethi.xrate.api.ConversionResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.Currency;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ResultPrinterTest {

    private static final Currency FROM = Currency.getInstance("USD");
    private static final Currency TO = Currency.getInstance("GBP");
    private static final BigDecimal AMOUNT = new BigDecimal("2");
    private static final BigDecimal RESULT = new BigDecimal("1.44");
    private static final ConversionResult CONVERSION_RESULT = new ConversionResult(FROM, TO, AMOUNT, RESULT);
    private static final String RESULT_FORMAT = "%s %s = %s %s";
    private static final String RATE_FORMAT = "1 %s = %s %s";


    @Test
    void print(@Mock PrintWriter writer) {
        new ResultPrinter().print(writer, CONVERSION_RESULT);

        verify(writer).println(formattedResult());
        verify(writer).println(formattedRate());
        verify(writer).println(formattedInverseRate());
    }

    private String formattedResult() {
        return String.format(RESULT_FORMAT, AMOUNT, FROM, RESULT, TO);
    }

    private String formattedRate() {
        return String.format(RATE_FORMAT, FROM, CONVERSION_RESULT.getRate(), TO);
    }

    private String formattedInverseRate() {
        return String.format(RATE_FORMAT, TO, CONVERSION_RESULT.getInverseRate(), FROM);
    }
}
