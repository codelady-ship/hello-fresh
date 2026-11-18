package io.github.codeladyship.hellofresh.repository;

import io.github.codeladyship.hellofresh.entity.MealKit;
import io.github.codeladyship.hellofresh.entity.Plan;
import io.github.codeladyship.hellofresh.entity.PlanRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Runs against H2 (PostgreSQL mode) with the real Flyway migrations, so it also checks that the
 * schema matches the entities ({@code ddl-auto=validate}) and that the page content was seeded.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class RepositoryTest {

    @Autowired
    private MealKitRepository mealKits;
    @Autowired
    private PlanRepository plans;
    @Autowired
    private PlanRequestRepository planRequests;
    @Autowired
    private TestEntityManager em;

    @Test
    void flywaySeedsTheFourMealKitsFromThePage() {
        var page = mealKits.findAll(PageRequest.of(0, 10, Sort.by("sortOrder")));

        assertThat(page.getTotalElements()).isEqualTo(4);
        assertThat(page.getContent()).extracting(MealKit::getTitle)
                .containsExactly("Meat & Veggies", "Veggie", "Family Friendly", "Fit & Wholesome");
        assertThat(page.getContent().getFirst().getImageUrl()).isEqualTo("images/page4-1.jpg");
    }

    @Test
    void findsMealKitsByTagCaseInsensitively() {
        var page = mealKits.findByTag("VEGETARIAN", PageRequest.of(0, 10));

        assertThat(page.getContent()).extracting(MealKit::getSlug).containsExactly("veggie");
        assertThat(page.getTotalElements()).isEqualTo(1);
    }

    @Test
    void cheapestPlanMatchesThePagePrice() {
        Plan plan = plans.findByPeopleAndMealsPerWeek(4, 5).orElseThrow();

        assertThat(plan.getPricePerServing()).isEqualByComparingTo("7.49");
        assertThat(plans.count()).isEqualTo(6);
        assertThat(plans.findByPeopleAndMealsPerWeek(3, 3)).isEmpty();
    }

    @Test
    void savesPlanRequestWithRelations() {
        Plan plan = plans.findByPeopleAndMealsPerWeek(2, 3).orElseThrow();
        MealKit kit = mealKits.findBySlug("veggie").orElseThrow();

        PlanRequest saved = planRequests.save(new PlanRequest("Ana", "ana@example.com", plan, kit,
                Instant.parse("2026-11-18T10:00:00Z")));
        em.flush();
        em.clear();

        PlanRequest found = planRequests.findById(saved.getId()).orElseThrow();
        assertThat(found.getWeeklyTotal()).isEqualByComparingTo("70.93");
        assertThat(found.getMealKit().getSlug()).isEqualTo("veggie");
        assertThat(found.getPlan().getMealsPerWeek()).isEqualTo(3);
    }
}
