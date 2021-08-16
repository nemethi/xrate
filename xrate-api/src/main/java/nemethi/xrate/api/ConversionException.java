package nemethi.xrate.api;

/**
 * Thrown to indicate that an error occurred during conversion.
 */
public class ConversionException extends RuntimeException {

    /**
     * Constructs a new instance with the specified message.
     *
     * @param message the message
     */
    public ConversionException(String message) {
        super(message);
    }
}
