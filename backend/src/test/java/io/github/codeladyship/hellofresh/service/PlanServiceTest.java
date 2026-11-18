package io.github.codeladyship.hellofresh.service;

import io.github.codeladyship.hellofresh.dto.PlanResponse;
import io.github.codeladyship.hellofresh.entity.Plan;
import io.github.codeladyship.hellofresh.exception.NotFoundException;
import io.github.codeladyship.hellofresh.repository.PlanRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlanServiceTest {

    @Mock
    private PlanRepository repository;

    @InjectMocks
    private PlanService service;

    @Test
    void quoteCalculatesServingsAndWeeklyTotal() {
        when(repository.findByPeopleAndMealsPerWeek(4, 4))
                .thenReturn(Optional.of(new Plan(4, 4, new BigDecimal("7.99"), new BigDecimal("10.99"))));

        PlanResponse quote = service.quote(4, 4);

        assertThat(quote.servingsPerWeek()).isEqualTo(16);
        // 16 x 7.99 + 10.99 shipping
        assertThat(quote.weeklyTotal()).isEqualByComparingTo("138.83");
    }

    @Test
    void quoteForMissingPlanThrowsNotFound() {
        when(repository.findByPeopleAndMealsPerWeek(1, 1)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.quote(1, 1)).isInstanceOf(NotFoundException.class);
    }

    @Test
    void listMapsPage() {
        PageRequest pageable = PageRequest.of(0, 2);
        when(repository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(
                new Plan(2, 3, new BigDecimal("9.99"), new BigDecimal("10.99")),
                new Plan(2, 4, new BigDecimal("9.49"), new BigDecimal("10.99"))), pageable, 6));

        var page = service.list(pageable);

        assertThat(page.content()).hasSize(2);
        assertThat(page.totalElements()).isEqualTo(6);
        assertThat(page.totalPages()).isEqualTo(3);
        assertThat(page.last()).isFalse();
        assertThat(page.content().get(1).weeklyTotal()).isEqualByComparingTo("86.91");
    }
}
