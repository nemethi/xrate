package nemethi.xrate.plugin;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class ExchangeRateApiExceptionTest {

    private static final String UNKNOWN_ERROR_MESSAGE = "Unknown error";

    @ParameterizedTest
    @MethodSource("arguments")
    void messageDependsOnErrorType(String errorType, String message) {
        var exception = new ExchangeRateApiException(errorType);
        assertThat(exception).hasMessage(message);
    }

    private static Stream<Arguments> arguments() {
        return Stream.of(
                Arguments.of(null, UNKNOWN_ERROR_MESSAGE),
                Arguments.of("", UNKNOWN_ERROR_MESSAGE),
                Arguments.of(" ", UNKNOWN_ERROR_MESSAGE),
                Arguments.of("unknown code", UNKNOWN_ERROR_MESSAGE),
                Arguments.of("unsupported-code", "The supplied currency code is not supported"),
                Arguments.of("malformed-request", "Some part of the request doesn't follow the expected structure"),
                Arguments.of("invalid-key", "Your API key is not valid"),
                Arguments.of("inactive-account", "Your email address wasn't confirmed"),
                Arguments.of("quota-reached", "Your account has reached the number of requests allowed by your plan"));
    }

}
