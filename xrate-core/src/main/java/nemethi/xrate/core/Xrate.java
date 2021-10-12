package nemethi.xrate.core;

import nemethi.xrate.api.ConversionResult;
import nemethi.xrate.api.CurrencyConverter;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.Optional;

public class Xrate {

    private final Configuration config;
    private final PluginLoader loader;
    private final ResultPrinter printer;

    public Xrate(Configuration config, PrintWriter writer) {
        this(config, new PluginLoader(), new ResultPrinter(writer));
    }

    Xrate(Configuration config, PluginLoader loader, ResultPrinter printer) {
        this.config = config;
        this.loader = loader;
        this.printer = printer;
    }

    public void convert(Currency from, Currency to, BigDecimal amount) {
        ConversionResult result = createConverter().convert(from, to, amount);
        printer.print(result);
    }

    private CurrencyConverter createConverter() {
        Optional<CurrencyConverter> plugin = loader.findFirstPlugin();
        if (plugin.isPresent()) {
            CurrencyConverter converter = plugin.get();
            converter.setAuthCredentials(config.getPluginAuthCredentials());
            return converter;
        } else {
            CurrConvApiClient client = createDefaultClient(config.getCurrConvEndpoint());
            return createDefaultConverter(client, config.getCoreAuthCredentials());
        }
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
