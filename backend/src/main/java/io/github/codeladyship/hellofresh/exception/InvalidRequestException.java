package io.github.codeladyship.hellofresh.exception;

/** Thrown when a request is well-formed but refers to something that cannot be used; mapped to 422. */
public class InvalidRequestException extends RuntimeException {

    private final String field;

    public InvalidRequestException(String field, String message) {
        super(message);
        this.field = field;
    }

    public String getField() {
        return field;
    }
}
