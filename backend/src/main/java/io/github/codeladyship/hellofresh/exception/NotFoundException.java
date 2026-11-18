package io.github.codeladyship.hellofresh.exception;

/** Thrown when a requested resource does not exist; mapped to 404 problem+json. */
public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }
}
