package io.github.codeladyship.hellofresh.service;

import io.github.codeladyship.hellofresh.dto.PageResponse;
import io.github.codeladyship.hellofresh.dto.PlanResponse;
import io.github.codeladyship.hellofresh.entity.Plan;
import io.github.codeladyship.hellofresh.exception.NotFoundException;
import io.github.codeladyship.hellofresh.repository.PlanRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class PlanService {

    private final PlanRepository repository;

    public PlanService(PlanRepository repository) {
        this.repository = repository;
    }

    public PageResponse<PlanResponse> list(Pageable pageable) {
        return PageResponse.of(repository.findAll(pageable), PlanResponse::from);
    }

    /** Price for a household size and number of meals per week. */
    public PlanResponse quote(int people, int mealsPerWeek) {
        return PlanResponse.from(findPlan(people, mealsPerWeek));
    }

    Plan findPlan(int people, int mealsPerWeek) {
        return repository.findByPeopleAndMealsPerWeek(people, mealsPerWeek)
                .orElseThrow(() -> new NotFoundException(
                        "No plan for " + people + " people and " + mealsPerWeek + " meals per week"));
    }
}
