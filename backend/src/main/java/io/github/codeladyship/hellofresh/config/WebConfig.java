package io.github.codeladyship.hellofresh.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.Clock;
import java.time.Duration;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final String[] allowedOriginPatterns;
    private final int planRequestsPerMinute;

    public WebConfig(@Value("${app.cors.allowed-origin-patterns}") String[] allowedOriginPatterns,
                     @Value("${app.rate-limit.plan-requests-per-minute:5}") int planRequestsPerMinute) {
        this.allowedOriginPatterns = allowedOriginPatterns;
        this.planRequestsPerMinute = planRequestsPerMinute;
    }

    /** Only the GitHub Pages site and localhost may call the API from a browser. */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns(allowedOriginPatterns)
                .allowedMethods("GET", "POST", "OPTIONS")
                .allowedHeaders("Content-Type", "Accept")
                .allowCredentials(false)
                .maxAge(3600);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new PlanRequestRateLimitInterceptor(planRequestRateLimiter(clock())))
                .addPathPatterns("/api/plan-requests", "/api/plan-requests/**");
    }

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }

    /** Per-IP limit for the public write endpoint (default 5 per minute). */
    @Bean
    public RateLimiter planRequestRateLimiter(Clock clock) {
        return new RateLimiter(planRequestsPerMinute, Duration.ofMinutes(1), clock);
    }
}
