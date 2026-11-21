package io.github.codeladyship.hellofresh.service;

import io.github.codeladyship.hellofresh.dto.PlanRequestCreate;
import io.github.codeladyship.hellofresh.dto.PlanRequestResponse;
import io.github.codeladyship.hellofresh.entity.MealKit;
import io.github.codeladyship.hellofresh.entity.Plan;
import io.github.codeladyship.hellofresh.entity.PlanRequest;
import io.github.codeladyship.hellofresh.entity.PlanRequestStatus;
import io.github.codeladyship.hellofresh.exception.InvalidRequestException;
import io.github.codeladyship.hellofresh.repository.MealKitRepository;
import io.github.codeladyship.hellofresh.repository.PlanRepository;
import io.github.codeladyship.hellofresh.repository.PlanRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlanRequestServiceTest {

    private static final Instant NOW = Instant.parse("2026-11-18T10:00:00Z");

    @Mock
    private PlanRequestRepository planRequests;
    @Mock
    private PlanRepository plans;
    @Mock
    private MealKitRepository mealKits;

    private PlanRequestService service;

    @BeforeEach
    void setUp() {
        service = new PlanRequestService(planRequests, plans, mealKits, Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @Test
    void storesRequestWithPriceSnapshotAndNormalisedEmail() {
        Plan plan = new Plan(2, 3, new BigDecimal("9.99"), new BigDecimal("10.99"));
        MealKit veggie = new MealKit("veggie", "Veggie", "& plant-based meals", "desc", "images/page4-2.jpg",
                25, 580, new BigDecimal("8.99"), 2, Set.of("vegetarian"));
        when(plans.findByPeopleAndMealsPerWeek(2, 3)).thenReturn(Optional.of(plan));
        when(mealKits.findBySlug("veggie")).thenReturn(Optional.of(veggie));
        when(planRequests.save(any(PlanRequest.class))).thenAnswer(inv -> inv.getArgument(0));

        PlanRequestResponse response = service.create(
                new PlanRequestCreate("  Ana  ", " Ana@Example.COM ", 2, 3, "veggie"));

        ArgumentCaptor<PlanRequest> saved = ArgumentCaptor.forClass(PlanRequest.class);
        verify(planRequests).save(saved.capture());
        assertThat(saved.getValue().getEmail()).isEqualTo("ana@example.com");
        assertThat(saved.getValue().getName()).isEqualTo("Ana");
        assertThat(saved.getValue().getCreatedAt()).isEqualTo(NOW);
        assertThat(response.status()).isEqualTo(PlanRequestStatus.RECEIVED);
        assertThat(response.weeklyTotal()).isEqualByComparingTo("70.93");
        assertThat(response.mealKitSlug()).isEqualTo("veggie");
    }

    @Test
    void mealKitIsOptional() {
        Plan plan = new Plan(4, 5, new BigDecimal("7.49"), new BigDecimal("10.99"));
        when(plans.findByPeopleAndMealsPerWeek(4, 5)).thenReturn(Optional.of(plan));
        when(planRequests.save(any(PlanRequest.class))).thenAnswer(inv -> inv.getArgument(0));

        PlanRequestResponse response = service.create(new PlanRequestCreate("Ana", "ana@example.com", 4, 5, null));

        assertThat(response.mealKitSlug()).isNull();
        assertThat(response.weeklyTotal()).isEqualByComparingTo("160.79");
        verify(mealKits, never()).findBySlug(any());
    }

    @Test
    void deleteOlderThanUsesClockMinusRetention() {
        when(planRequests.deleteByCreatedAtBefore(Instant.parse("2026-10-19T10:00:00Z"))).thenReturn(3);

        assertThat(service.deleteOlderThan(java.time.Duration.ofDays(30))).isEqualTo(3);
    }

    @Test
    void rejectsUnavailablePlan() {
        when(plans.findByPeopleAndMealsPerWeek(3, 3)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(new PlanRequestCreate("Ana", "ana@example.com", 3, 3, null)))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("3 meals per week for 3 people");
        verify(planRequests, never()).save(any());
    }

    @Test
    void rejectsUnknownMealKit() {
        when(plans.findByPeopleAndMealsPerWeek(2, 3))
                .thenReturn(Optional.of(new Plan(2, 3, new BigDecimal("9.99"), new BigDecimal("10.99"))));
        when(mealKits.findBySlug("pizza")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(new PlanRequestCreate("Ana", "ana@example.com", 2, 3, "pizza")))
                .isInstanceOf(InvalidRequestException.class)
                .extracting("field").isEqualTo("mealKitSlug");
        verify(planRequests, never()).save(any());
    }
}
