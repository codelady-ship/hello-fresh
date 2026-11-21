package io.github.codeladyship.hellofresh.config;

import io.github.codeladyship.hellofresh.service.PlanRequestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.Duration;

/**
 * Scheduled housekeeping. Runs one minute after start-up and then every 6 hours, so it also runs
 * after a free Render instance wakes up (a fixed night-time cron could be missed while it sleeps).
 */
@Configuration
@EnableScheduling
@ConditionalOnProperty(name = "app.jobs.enabled", havingValue = "true", matchIfMissing = true)
public class MaintenanceJobs {

    private static final Logger log = LoggerFactory.getLogger(MaintenanceJobs.class);

    private final PlanRequestService planRequests;
    private final RateLimiter rateLimiter;
    private final Duration retention;

    public MaintenanceJobs(PlanRequestService planRequests, RateLimiter rateLimiter,
                           @Value("${app.retention.plan-request-days:30}") int retentionDays) {
        this.planRequests = planRequests;
        this.rateLimiter = rateLimiter;
        this.retention = Duration.ofDays(retentionDays);
    }

    /** Deletes plan requests (names and emails) older than the retention period (default 30 days). */
    @Scheduled(initialDelayString = "PT1M", fixedDelayString = "PT6H")
    public void deleteOldPlanRequests() {
        int deleted = planRequests.deleteOlderThan(retention);
        if (deleted > 0) {
            log.info("Deleted {} plan requests older than {} days", deleted, retention.toDays());
        }
    }

    @Scheduled(initialDelayString = "PT5M", fixedDelayString = "PT5M")
    public void evictRateLimitWindows() {
        rateLimiter.evictExpired();
    }
}
