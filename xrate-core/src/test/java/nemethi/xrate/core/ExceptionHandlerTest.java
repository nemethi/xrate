package nemethi.xrate.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import picocli.CommandLine;
import picocli.CommandLine.Help.Ansi.Text;
import picocli.CommandLine.Help.ColorScheme;
import picocli.CommandLine.IExitCodeExceptionMapper;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.ParseResult;

import java.io.PrintWriter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExceptionHandlerTest {

    private static final String COMMAND_NAME = "testCommandName";
    private static final String EXCEPTION_MESSAGE = "testExceptionMessage";
    private static final String CAUSE_MESSAGE = "testCauseMessage";
    private static final String ERROR_MESSAGE = String.format("%s: %s", COMMAND_NAME, EXCEPTION_MESSAGE);
    private static final String ERROR_MESSAGE_WITH_CAUSE = String.format("%s: %s%n%s", COMMAND_NAME, EXCEPTION_MESSAGE, CAUSE_MESSAGE);
    private static final int INVALID_PARAMETER_EXIT_CODE = 2;
    private static final int EXECUTION_EXCEPTION_EXIT_CODE = 1;
    private static final String[] EMPTY_ARRAY = new String[0];

    @Mock
    private ParameterException parameterException;
    @Mock
    private Exception executionException, cause;
    @Mock
    private CommandLine commandLine;
    @Mock
    private PrintWriter outWriter, errorWriter;
    @Mock
    private ColorScheme colorScheme;
    @Mock
    private IExitCodeExceptionMapper exceptionMapper;
    @Mock
    private CommandSpec commandSpec;
    @Mock(stubOnly = true)
    private Text text;
    @Mock(stubOnly = true)
    private ParseResult parseResult;

    @Spy
    private ExceptionHandler exceptionHandler;

    @Nested
    class ParseExceptionHandlingTests {

        @Test
        @DisplayName("Handle parse exception without printing suggestions " +
                "and returning the exit code provided by the exception mapper")
        void withoutSuggestionsAndWithExceptionMapper() {
            when(parameterException.getCommandLine()).thenReturn(commandLine);
            when(commandLine.getCommandName()).thenReturn(COMMAND_NAME);
            when(parameterException.getMessage()).thenReturn(EXCEPTION_MESSAGE);
            when(commandLine.getErr()).thenReturn(errorWriter);
            when(commandLine.getColorScheme()).thenReturn(colorScheme);
            when(colorScheme.errorText(anyString())).thenReturn(text);
            doReturn(false).when(exceptionHandler).printSuggestions(any(), any());
            when(commandLine.getOut()).thenReturn(outWriter);
            when(commandLine.getExitCodeExceptionMapper()).thenReturn(exceptionMapper);
            when(exceptionMapper.getExitCode(any())).thenReturn(INVALID_PARAMETER_EXIT_CODE);

            int exitCode = exceptionHandler.handleParseException(parameterException, EMPTY_ARRAY);

            assertThat(exitCode).isEqualTo(INVALID_PARAMETER_EXIT_CODE);
            verify(parameterException).getCommandLine();
            verify(commandLine).getCommandName();
            verify(parameterException).getMessage();
            verify(commandLine).getErr();
            verify(commandLine, times(2)).getColorScheme();
            verify(colorScheme).errorText(ERROR_MESSAGE);
            verify(exceptionHandler).printSuggestions(parameterException, commandLine);
            verify(errorWriter).println(text);
            verify(commandLine).getOut();
            verify(commandLine).usage(outWriter, colorScheme);
            verify(commandLine).getExitCodeExceptionMapper();
            verify(exceptionMapper).getExitCode(parameterException);
        }

        @Test
        @DisplayName("Handle parse exception without printing suggestions " +
                "and returning the exit code for invalid input")
        void withoutSuggestionsAndWithoutExceptionMapper() {
            when(parameterException.getCommandLine()).thenReturn(commandLine);
            when(commandLine.getCommandName()).thenReturn(COMMAND_NAME);
            when(parameterException.getMessage()).thenReturn(EXCEPTION_MESSAGE);
            when(commandLine.getErr()).thenReturn(errorWriter);
            when(commandLine.getColorScheme()).thenReturn(colorScheme);
            when(colorScheme.errorText(anyString())).thenReturn(text);
            doReturn(false).when(exceptionHandler).printSuggestions(any(), any());
            when(commandLine.getOut()).thenReturn(outWriter);
            when(commandLine.getExitCodeExceptionMapper()).thenReturn(null);
            when(commandLine.getCommandSpec()).thenReturn(commandSpec);
            when(commandSpec.exitCodeOnInvalidInput()).thenReturn(INVALID_PARAMETER_EXIT_CODE);

            int exitCode = exceptionHandler.handleParseException(parameterException, EMPTY_ARRAY);

            assertThat(exitCode).isEqualTo(INVALID_PARAMETER_EXIT_CODE);
            verify(parameterException).getCommandLine();
            verify(commandLine).getCommandName();
            verify(parameterException).getMessage();
            verify(commandLine).getErr();
            verify(commandLine, times(2)).getColorScheme();
            verify(colorScheme).errorText(ERROR_MESSAGE);
            verify(exceptionHandler).printSuggestions(parameterException, commandLine);
            verify(errorWriter).println(text);
            verify(commandLine).getOut();
            verify(commandLine).usage(outWriter, colorScheme);
            verify(commandLine).getExitCodeExceptionMapper();
            verify(commandLine).getCommandSpec();
            verify(commandSpec).exitCodeOnInvalidInput();
        }

        @Test
        @DisplayName("Handle parse exception with printing suggestions " +
                "and returning the exit code provided by the exception mapper")
        void withSuggestionsAndWithExceptionMapper() {
            when(parameterException.getCommandLine()).thenReturn(commandLine);
            when(commandLine.getCommandName()).thenReturn(COMMAND_NAME);
            when(parameterException.getMessage()).thenReturn(EXCEPTION_MESSAGE);
            when(commandLine.getErr()).thenReturn(errorWriter);
            when(commandLine.getColorScheme()).thenReturn(colorScheme);
            when(colorScheme.errorText(anyString())).thenReturn(text);
            doReturn(true).when(exceptionHandler).printSuggestions(any(), any());
            when(commandLine.getExitCodeExceptionMapper()).thenReturn(exceptionMapper);
            when(exceptionMapper.getExitCode(any())).thenReturn(INVALID_PARAMETER_EXIT_CODE);

            int exitCode = exceptionHandler.handleParseException(parameterException, EMPTY_ARRAY);

            assertThat(exitCode).isEqualTo(INVALID_PARAMETER_EXIT_CODE);
            verify(parameterException).getCommandLine();
            verify(commandLine).getCommandName();
            verify(parameterException).getMessage();
            verify(commandLine).getErr();
            verify(commandLine).getColorScheme();
            verify(colorScheme).errorText(ERROR_MESSAGE);
            verify(exceptionHandler).printSuggestions(parameterException, commandLine);
            verify(commandLine).getExitCodeExceptionMapper();
            verify(exceptionMapper).getExitCode(parameterException);
        }

        @Test
        @DisplayName("Handler parse exception with printing suggestions " +
                "and returning the exit code for invalid input")
        void withSuggestionsAndWithoutExceptionMapper() {
            when(parameterException.getCommandLine()).thenReturn(commandLine);
            when(commandLine.getCommandName()).thenReturn(COMMAND_NAME);
            when(parameterException.getMessage()).thenReturn(EXCEPTION_MESSAGE);
            when(commandLine.getErr()).thenReturn(errorWriter);
            when(commandLine.getColorScheme()).thenReturn(colorScheme);
            when(colorScheme.errorText(anyString())).thenReturn(text);
            doReturn(true).when(exceptionHandler).printSuggestions(any(), any());
            when(commandLine.getExitCodeExceptionMapper()).thenReturn(null);
            when(commandLine.getCommandSpec()).thenReturn(commandSpec);
            when(commandSpec.exitCodeOnInvalidInput()).thenReturn(INVALID_PARAMETER_EXIT_CODE);

            int exitCode = exceptionHandler.handleParseException(parameterException, EMPTY_ARRAY);

            assertThat(exitCode).isEqualTo(INVALID_PARAMETER_EXIT_CODE);
            verify(parameterException).getCommandLine();
            verify(commandLine).getCommandName();
            verify(parameterException).getMessage();
            verify(commandLine).getErr();
            verify(commandLine).getColorScheme();
            verify(colorScheme).errorText(ERROR_MESSAGE);
            verify(exceptionHandler).printSuggestions(parameterException, commandLine);
            verify(commandLine).getExitCodeExceptionMapper();
            verify(commandLine).getCommandSpec();
            verify(commandSpec).exitCodeOnInvalidInput();
        }
    }

    @Nested
    class ExecutionExceptionHandlingTests {

        @Test
        @DisplayName("Handle execution exception and return the exit code" +
                " provided by the exception mapper")
        void handleExecutionExceptionWithExceptionMapper() {
            when(executionException.getMessage()).thenReturn(EXCEPTION_MESSAGE);
            when(commandLine.getCommandName()).thenReturn(COMMAND_NAME);
            when(commandLine.getErr()).thenReturn(errorWriter);
            when(commandLine.getColorScheme()).thenReturn(colorScheme);
            when(colorScheme.errorText(anyString())).thenReturn(text);
            when(commandLine.getExitCodeExceptionMapper()).thenReturn(exceptionMapper);
            when(exceptionMapper.getExitCode(any())).thenReturn(EXECUTION_EXCEPTION_EXIT_CODE);

            int exitCode = exceptionHandler.handleExecutionException(executionException, commandLine, parseResult);

            assertThat(exitCode).isEqualTo(EXECUTION_EXCEPTION_EXIT_CODE);
            verify(executionException).getMessage();
            verify(commandLine).getCommandName();
            verify(commandLine).getErr();
            verify(commandLine).getColorScheme();
            verify(colorScheme).errorText(ERROR_MESSAGE);
            verify(errorWriter).println(text);
            verify(commandLine).getExitCodeExceptionMapper();
            verify(exceptionMapper).getExitCode(executionException);
        }

        @Test
        @DisplayName("Handle execution exception and return the exit code for execution error")
        void handleExecutionExceptionWithoutExceptionMapper() {
            when(executionException.getCause()).thenReturn(cause);
            when(executionException.getMessage()).thenReturn(EXCEPTION_MESSAGE);
            when(commandLine.getCommandName()).thenReturn(COMMAND_NAME);
            when(commandLine.getErr()).thenReturn(errorWriter);
            when(commandLine.getColorScheme()).thenReturn(colorScheme);
            when(colorScheme.errorText(anyString())).thenReturn(text);
            when(commandLine.getExitCodeExceptionMapper()).thenReturn(null);
            when(commandLine.getCommandSpec()).thenReturn(commandSpec);
            when(commandSpec.exitCodeOnExecutionException()).thenReturn(EXECUTION_EXCEPTION_EXIT_CODE);

            int exitCode = exceptionHandler.handleExecutionException(executionException, commandLine, parseResult);

            assertThat(exitCode).isEqualTo(EXECUTION_EXCEPTION_EXIT_CODE);
            verify(executionException).getCause();
            verify(executionException).getMessage();
            verify(commandLine).getCommandName();
            verify(commandLine).getErr();
            verify(commandLine).getColorScheme();
            verify(colorScheme).errorText(ERROR_MESSAGE);
            verify(errorWriter).println(text);
            verify(commandLine).getExitCodeExceptionMapper();
            verify(commandLine).getCommandSpec();
            verify(commandSpec).exitCodeOnExecutionException();
        }

        @Test
        @DisplayName("Handle execution exception that has a cause and return the exit code" +
                " provided by the exception mapper")
        void handleExecutionExceptionWithCauseAndWithExceptionMapper() {
            when(executionException.getCause()).thenReturn(cause);
            when(cause.getMessage()).thenReturn(CAUSE_MESSAGE);
            when(executionException.getMessage()).thenReturn(EXCEPTION_MESSAGE);
            when(commandLine.getCommandName()).thenReturn(COMMAND_NAME);
            when(commandLine.getErr()).thenReturn(errorWriter);
            when(commandLine.getColorScheme()).thenReturn(colorScheme);
            when(colorScheme.errorText(anyString())).thenReturn(text);
            when(commandLine.getExitCodeExceptionMapper()).thenReturn(exceptionMapper);
            when(exceptionMapper.getExitCode(any())).thenReturn(EXECUTION_EXCEPTION_EXIT_CODE);

            int exitCode = exceptionHandler.handleExecutionException(executionException, commandLine, parseResult);

            assertThat(exitCode).isEqualTo(EXECUTION_EXCEPTION_EXIT_CODE);
            verify(executionException, times(2)).getCause();
            verify(cause, times(2)).getMessage();
            verify(executionException).getMessage();
            verify(commandLine).getCommandName();
            verify(commandLine).getErr();
            verify(commandLine).getColorScheme();
            verify(colorScheme).errorText(ERROR_MESSAGE_WITH_CAUSE);
            verify(errorWriter).println(text);
            verify(commandLine).getExitCodeExceptionMapper();
            verify(exceptionMapper).getExitCode(executionException);
        }

        @Test
        @DisplayName("Handle execution exception that has a cause" +
                " and return the exit code for execution error")
        void handleExecutionExceptionWithCauseAndWithoutExceptionMapper() {
            when(executionException.getCause()).thenReturn(cause);
            when(cause.getMessage()).thenReturn(CAUSE_MESSAGE);
            when(executionException.getMessage()).thenReturn(EXCEPTION_MESSAGE);
            when(commandLine.getCommandName()).thenReturn(COMMAND_NAME);
            when(commandLine.getErr()).thenReturn(errorWriter);
            when(commandLine.getColorScheme()).thenReturn(colorScheme);
            when(colorScheme.errorText(anyString())).thenReturn(text);
            when(commandLine.getExitCodeExceptionMapper()).thenReturn(null);
            when(commandLine.getCommandSpec()).thenReturn(commandSpec);
            when(commandSpec.exitCodeOnExecutionException()).thenReturn(EXECUTION_EXCEPTION_EXIT_CODE);

            int exitCode = exceptionHandler.handleExecutionException(executionException, commandLine, parseResult);

            assertThat(exitCode).isEqualTo(EXECUTION_EXCEPTION_EXIT_CODE);
            verify(executionException, times(2)).getCause();
            verify(cause, times(2)).getMessage();
            verify(executionException).getMessage();
            verify(commandLine).getCommandName();
            verify(commandLine).getErr();
            verify(commandLine).getColorScheme();
            verify(colorScheme).errorText(ERROR_MESSAGE_WITH_CAUSE);
            verify(commandLine).getExitCodeExceptionMapper();
            verify(commandLine).getCommandSpec();
            verify(commandSpec).exitCodeOnExecutionException();
        }
    }
}
