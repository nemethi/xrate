package nemethi.xrate.core;

import nemethi.xrate.api.ConversionResult;
import nemethi.xrate.api.CurrencyConverter;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.Currency;

public class Xrate {

    private final Configuration config;
    private final ResultPrinter printer;

    public Xrate(Configuration config, PrintWriter writer) {
        this(config, new ResultPrinter(writer));
    }

    Xrate(Configuration config, ResultPrinter printer) {
        this.config = config;
        this.printer = printer;
    }

    public void convert(Currency from, Currency to, BigDecimal amount) {
        CurrConvApiClient client = createDefaultClient(config.getCurrConvEndpoint());
        CurrencyConverter converter = createDefaultConverter(client, config.getCoreAuthCredentials());
        ConversionResult result = converter.convert(from, to, amount);
        printer.print(result);
    }

    private CurrConvApiClient createDefaultClient(String endpoint) {
        return new CurrConvApiClient(endpoint);
    }

    CurrencyConverter createDefaultConverter(CurrConvApiClient client, String authCredentials) {
        DefaultCurrencyConverter converter = new DefaultCurrencyConverter(client);
        converter.setAuthCredentials(authCredentials);
        return converter;
    }
}
