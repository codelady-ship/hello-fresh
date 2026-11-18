package io.github.codeladyship.hellofresh.dto;

import io.github.codeladyship.hellofresh.entity.PlanRequest;
import io.github.codeladyship.hellofresh.entity.PlanRequestStatus;

import java.math.BigDecimal;
import java.time.Instant;

/** What the API returns after a plan request is stored. The email is not echoed back. */
public record PlanRequestResponse(
        Long id,
        PlanRequestStatus status,
        String name,
        int people,
        int mealsPerWeek,
        String mealKitSlug,
        BigDecimal weeklyTotal,
        Instant createdAt) {

    public static PlanRequestResponse from(PlanRequest request) {
        return new PlanRequestResponse(
                request.getId(),
                request.getStatus(),
                request.getName(),
                request.getPlan().getPeople(),
                request.getPlan().getMealsPerWeek(),
                request.getMealKit() == null ? null : request.getMealKit().getSlug(),
                request.getWeeklyTotal(),
                request.getCreatedAt());
    }
}
