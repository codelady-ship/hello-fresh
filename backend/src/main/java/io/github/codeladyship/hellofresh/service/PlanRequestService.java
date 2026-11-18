package io.github.codeladyship.hellofresh.service;

import io.github.codeladyship.hellofresh.dto.PlanRequestCreate;
import io.github.codeladyship.hellofresh.dto.PlanRequestResponse;
import io.github.codeladyship.hellofresh.entity.MealKit;
import io.github.codeladyship.hellofresh.entity.Plan;
import io.github.codeladyship.hellofresh.entity.PlanRequest;
import io.github.codeladyship.hellofresh.exception.InvalidRequestException;
import io.github.codeladyship.hellofresh.repository.MealKitRepository;
import io.github.codeladyship.hellofresh.repository.PlanRepository;
import io.github.codeladyship.hellofresh.repository.PlanRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Clock;
import java.time.Instant;
import java.util.Locale;

@Service
public class PlanRequestService {

    private final PlanRequestRepository planRequests;
    private final PlanRepository plans;
    private final MealKitRepository mealKits;
    private final Clock clock;

    public PlanRequestService(PlanRequestRepository planRequests, PlanRepository plans,
                              MealKitRepository mealKits, Clock clock) {
        this.planRequests = planRequests;
        this.plans = plans;
        this.mealKits = mealKits;
        this.clock = clock;
    }

    /** Stores a plan request with a snapshot of the weekly price. */
    @Transactional
    public PlanRequestResponse create(PlanRequestCreate body) {
        Plan plan = plans.findByPeopleAndMealsPerWeek(body.people(), body.mealsPerWeek())
                .orElseThrow(() -> new InvalidRequestException("mealsPerWeek",
                        "We do not offer " + body.mealsPerWeek() + " meals per week for "
                                + body.people() + " people yet"));

        MealKit mealKit = null;
        if (StringUtils.hasText(body.mealKitSlug())) {
            mealKit = mealKits.findBySlug(body.mealKitSlug())
                    .orElseThrow(() -> new InvalidRequestException("mealKitSlug",
                            "Unknown meal kit '" + body.mealKitSlug() + "'"));
        }

        PlanRequest request = new PlanRequest(
                body.name().trim(),
                body.email().trim().toLowerCase(Locale.ROOT),
                plan,
                mealKit,
                Instant.now(clock));
        return PlanRequestResponse.from(planRequests.save(request));
    }
}
