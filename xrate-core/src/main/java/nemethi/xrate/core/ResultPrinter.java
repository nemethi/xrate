package nemethi.xrate.core;

import nemethi.xrate.api.ConversionResult;

import java.io.PrintWriter;

public class ResultPrinter {

    private static final String RESULT_FORMAT = "%s %s = %s %s";
    private static final String RATE_FORMAT = "1 %s = %s %s";

    private final PrintWriter writer;

    public ResultPrinter(PrintWriter writer) {
        this.writer = writer;
    }

    public void print(ConversionResult result) {
        writer.println(String.format(RESULT_FORMAT, result.getAmount(), result.getFrom(), result.getResult(), result.getTo()));
        writer.println(String.format(RATE_FORMAT, result.getFrom(), result.getRate(), result.getTo()));
        writer.println(String.format(RATE_FORMAT, result.getTo(), result.getInverseRate(), result.getFrom()));
    }
}
