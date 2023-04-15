package nemethi.xrate.core;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.concurrent.Callable;

import static java.util.stream.Collectors.joining;

@Command(name = "xrate", description = "Get exchange rates and convert currencies using third-party services.%n",
        mixinStandardHelpOptions = true, version = "1.0.1")
public class Application implements Callable<Integer> {

    private static final String DEFAULT_CONFIG_FILE = "xrate.properties";
    private static final String DEFAULT_AMOUNT = "1";
    private static final String DEFAULT_FROM_CURRENCY = "USD";
    private static final String DEFAULT_TO_CURRENCY = "GBP";
    private static final String CURRENCY_DELIMITER = ", ";

    private Currency from;
    private Currency to;
    private BigDecimal amount;
    private boolean listAvailableCurrencies;
    private String configFilePath;
    private CommandSpec spec;

    public static void main(String[] args) {
        Application application = new Application();
        ExceptionHandler exceptionHandler = new ExceptionHandler();
        int exitCode = new CommandLine(application)
                .setParameterExceptionHandler(exceptionHandler)
                .setExecutionExceptionHandler(exceptionHandler)
                .execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() {
        if (listAvailableCurrencies) {
            printAvailableCurrencies();
        } else {
            Xrate xrate = createXrate(getConfig(configFilePath), spec.commandLine().getOut());
            xrate.convert(from, to, amount);
        }
        return 0;
    }

    private void printAvailableCurrencies() {
        String currencies = Currency.getAvailableCurrencies()
                .stream()
                .map(Currency::getCurrencyCode)
                .sorted()
                .collect(joining(CURRENCY_DELIMITER));
        spec.commandLine().getOut().println(currencies);
    }

    Configuration getConfig(String configFilePath) {
        return new Configuration(configFilePath);
    }

    Xrate createXrate(Configuration config, PrintWriter writer) {
        return new Xrate(config, writer);
    }

    @Parameters(index = "0", paramLabel = "FROM",
            description = "The currency to convert from. Defaults to ${DEFAULT-VALUE}.", defaultValue = DEFAULT_FROM_CURRENCY)
    public void setFrom(String from) {
        this.from = validateCurrency(from);
    }

    @Parameters(index = "1", paramLabel = "TO",
            description = "The currency to convert to. Defaults to ${DEFAULT-VALUE}.", defaultValue = DEFAULT_TO_CURRENCY)
    public void setTo(String to) {
        this.to = validateCurrency(to);
    }

    private Currency validateCurrency(String code) {
        try {
            return Currency.getInstance(code);
        } catch (Exception e) {
            throw new ParameterException(spec.commandLine(), String.format("Unknown currency: %s", code));
        }
    }

    @Parameters(index = "2", paramLabel = "AMOUNT",
            description = "The amount to convert. Defaults to ${DEFAULT-VALUE}.", defaultValue = DEFAULT_AMOUNT)
    public void setAmount(String amount) {
        try {
            this.amount = new BigDecimal(amount);
        } catch (Exception e) {
            throw new ParameterException(spec.commandLine(), String.format("Invalid amount: %s", amount));
        }
    }

    @Option(names = {"-l", "--list"}, description = "Print the available currencies and exit.")
    public void setListAvailableCurrencies(boolean listAvailableCurrencies) {
        this.listAvailableCurrencies = listAvailableCurrencies;
    }

    @Option(names = {"-c", "--config"}, description = "Path to the config file to use.", defaultValue = DEFAULT_CONFIG_FILE)
    public void setConfigFilePath(String configFilePath) {
        this.configFilePath = configFilePath;
    }

    @Spec
    public void setSpec(CommandSpec spec) {
        this.spec = spec;
    }
}
