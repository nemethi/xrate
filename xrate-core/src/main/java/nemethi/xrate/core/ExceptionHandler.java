package nemethi.xrate.core;

import picocli.CommandLine;
import picocli.CommandLine.IExecutionExceptionHandler;
import picocli.CommandLine.IExitCodeExceptionMapper;
import picocli.CommandLine.IParameterExceptionHandler;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.ParseResult;
import picocli.CommandLine.UnmatchedArgumentException;

import static java.util.Objects.nonNull;

public class ExceptionHandler implements IParameterExceptionHandler, IExecutionExceptionHandler {

    private static final String ERROR_MESSAGE_FORMAT = "%s: %s";
    private static final String ERROR_MESSAGE_WITH_CAUSE_FORMAT = "%s%n%s";

    @Override
    public int handleParseException(ParameterException exception, String[] args) {
        CommandLine commandLine = exception.getCommandLine();
        printErrorMessage(exception.getMessage(), commandLine);
        printUsage(exception, commandLine);
        return getExitCodeForInvalidInput(exception, commandLine);
    }

    private void printUsage(ParameterException exception, CommandLine commandLine) {
        boolean suggestionDoesNotExist = !printSuggestions(exception, commandLine);
        if (suggestionDoesNotExist) {
            commandLine.usage(commandLine.getOut(), commandLine.getColorScheme());
        }
    }

    boolean printSuggestions(ParameterException exception, CommandLine commandLine) {
        return UnmatchedArgumentException.printSuggestions(exception, commandLine.getErr());
    }

    private int getExitCodeForInvalidInput(Exception exception, CommandLine commandLine) {
        IExitCodeExceptionMapper exceptionMapper = commandLine.getExitCodeExceptionMapper();
        if (nonNull(exceptionMapper)) {
            return exceptionMapper.getExitCode(exception);
        } else {
            return commandLine.getCommandSpec().exitCodeOnInvalidInput();
        }
    }

    @Override
    public int handleExecutionException(Exception exception, CommandLine commandLine, ParseResult parseResult) {
        if (causeMessageIsNonNull(exception.getCause())) {
            String message = String.format(ERROR_MESSAGE_WITH_CAUSE_FORMAT, exception.getMessage(),
                    exception.getCause().getMessage());
            printErrorMessage(message, commandLine);
        } else {
            printErrorMessage(exception.getMessage(), commandLine);
        }
        return getExitCodeForExecutionException(exception, commandLine);
    }

    private boolean causeMessageIsNonNull(Throwable cause) {
        return nonNull(cause) && nonNull(cause.getMessage());
    }

    private void printErrorMessage(String message, CommandLine commandLine) {
        String errorMessage = String.format(ERROR_MESSAGE_FORMAT, commandLine.getCommandName(), message);
        commandLine.getErr().println(commandLine.getColorScheme().errorText(errorMessage));
    }

    private int getExitCodeForExecutionException(Exception exception, CommandLine commandLine) {
        IExitCodeExceptionMapper exceptionMapper = commandLine.getExitCodeExceptionMapper();
        if (nonNull(exceptionMapper)) {
            return exceptionMapper.getExitCode(exception);
        } else {
            return commandLine.getCommandSpec().exitCodeOnExecutionException();
        }
    }
}
