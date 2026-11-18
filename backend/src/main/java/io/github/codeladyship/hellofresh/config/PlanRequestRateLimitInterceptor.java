package io.github.codeladyship.hellofresh.config;

import io.github.codeladyship.hellofresh.exception.RateLimitExceededException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

/** Limits POST /api/plan-requests per client IP; over the limit it answers 429 problem+json. */
public class PlanRequestRateLimitInterceptor implements HandlerInterceptor {

    private final RateLimiter limiter;

    public PlanRequestRateLimitInterceptor(RateLimiter limiter) {
        this.limiter = limiter;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        long retryAfter = limiter.tryAcquire(clientIp(request));
        if (retryAfter > 0) {
            throw new RateLimitExceededException(limiter.getLimit(), retryAfter);
        }
        return true;
    }

    /**
     * On Render the socket address is the load balancer, and the client address is the first
     * X-Forwarded-For entry. That entry can be forged by a determined client, so this is a basic
     * throttle against casual spam, not a security boundary. Without the header (local runs)
     * the socket address is used.
     */
    static String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(forwarded)) {
            String first = forwarded.split(",")[0].trim();
            if (!first.isEmpty()) {
                return first;
            }
        }
        return request.getRemoteAddr();
    }
}
