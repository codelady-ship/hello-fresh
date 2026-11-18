package io.github.codeladyship.hellofresh.exception;

/** Thrown when a client sends too many plan requests; mapped to 429 problem+json with Retry-After. */
public class RateLimitExceededException extends RuntimeException {

    private final long retryAfterSeconds;

    public RateLimitExceededException(int limit, long retryAfterSeconds) {
        super("Too many requests: at most " + limit + " plan requests per minute. Please try again in "
                + retryAfterSeconds + " seconds.");
        this.retryAfterSeconds = retryAfterSeconds;
    }

    public long getRetryAfterSeconds() {
        return retryAfterSeconds;
    }
}
