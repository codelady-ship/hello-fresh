package io.github.codeladyship.hellofresh.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Body of POST /api/plan-requests (the "Flexible Plans" form). */
public record PlanRequestCreate(
        @NotBlank(message = "Please enter your name")
        @Size(max = 80, message = "Name must be at most 80 characters")
        String name,

        @NotBlank(message = "Please enter your email")
        @Email(message = "Please enter a valid email address")
        @Size(max = 254, message = "Email must be at most 254 characters")
        String email,

        @NotNull(message = "Please choose the number of people")
        @Min(value = 1, message = "People must be at least 1")
        @Max(value = 10, message = "People must be at most 10")
        Integer people,

        @NotNull(message = "Please choose the number of meals per week")
        @Min(value = 1, message = "Meals per week must be at least 1")
        @Max(value = 7, message = "Meals per week must be at most 7")
        Integer mealsPerWeek,

        @Size(max = 80)
        @Pattern(regexp = "^[a-z0-9-]*$", message = "Meal kit must be a slug such as 'veggie'")
        String mealKitSlug) {
}
