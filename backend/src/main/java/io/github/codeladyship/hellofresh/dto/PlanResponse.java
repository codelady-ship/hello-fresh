package io.github.codeladyship.hellofresh.dto;

import io.github.codeladyship.hellofresh.entity.Plan;

import java.math.BigDecimal;

public record PlanResponse(
        Long id,
        int people,
        int mealsPerWeek,
        int servingsPerWeek,
        BigDecimal pricePerServing,
        BigDecimal shippingFee,
        BigDecimal weeklyTotal) {

    public static PlanResponse from(Plan plan) {
        return new PlanResponse(
                plan.getId(),
                plan.getPeople(),
                plan.getMealsPerWeek(),
                plan.servingsPerWeek(),
                plan.getPricePerServing(),
                plan.getShippingFee(),
                plan.weeklyTotal());
    }
}
