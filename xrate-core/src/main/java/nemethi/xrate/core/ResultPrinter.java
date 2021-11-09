package nemethi.xrate.core;

import nemethi.xrate.api.ConversionResult;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.text.DecimalFormat;

public class ResultPrinter {

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#,###.####");
    private static final String RESULT_FORMAT = "%s %s = %s %s";
    private static final String RATE_FORMAT = "1 %s = %s %s";

    private final PrintWriter writer;

    public ResultPrinter(PrintWriter writer) {
        this.writer = writer;
    }

    public void print(ConversionResult result) {
        writer.println(String.format(RESULT_FORMAT, format(result.getAmount()), result.getFrom(), format(result.getResult()), result.getTo()));
        writer.println(String.format(RATE_FORMAT, result.getFrom(), format(result.getRate()), result.getTo()));
        writer.println(String.format(RATE_FORMAT, result.getTo(), format(result.getInverseRate()), result.getFrom()));
    }

    private String format(BigDecimal amount) {
        return DECIMAL_FORMAT.format(amount);
    }
}
