package io.github.codeladyship.hellofresh.dto;

import io.github.codeladyship.hellofresh.entity.MealKit;

import java.math.BigDecimal;
import java.util.List;

public record MealKitResponse(
        Long id,
        String slug,
        String title,
        String subtitle,
        String description,
        String imageUrl,
        int cookingTimeMinutes,
        int calories,
        BigDecimal pricePerServing,
        List<String> tags) {

    public static MealKitResponse from(MealKit kit) {
        return new MealKitResponse(
                kit.getId(),
                kit.getSlug(),
                kit.getTitle(),
                kit.getSubtitle(),
                kit.getDescription(),
                kit.getImageUrl(),
                kit.getCookingTimeMinutes(),
                kit.getCalories(),
                kit.getPricePerServing(),
                kit.getTags().stream().sorted().toList());
    }
}
