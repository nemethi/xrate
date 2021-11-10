package nemethi.xrate.plugin;

import java.util.Map;

import static java.util.Objects.isNull;

public class ExchangeRateApiException extends Exception {
    private static final Map<String, String> ERROR_TYPES_TO_MESSAGES = Map.of(
            "unsupported-code", "The supplied currency code is not supported",
            "malformed-request", "Some part of the request doesn't follow the expected structure",
            "invalid-key", "Your API key is not valid",
            "inactive-account", "Your email address wasn't confirmed",
            "quota-reached", "Your account has reached the number of requests allowed by your plan");

    private final String errorType;

    public ExchangeRateApiException(String errorType) {
        if (isNull(errorType)) {
            this.errorType = "";
        } else {
            this.errorType = errorType;
        }
    }

    @Override
    public String getMessage() {
        return ERROR_TYPES_TO_MESSAGES.getOrDefault(errorType, "Unknown error");
    }
}
