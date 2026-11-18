package io.github.codeladyship.hellofresh.controller;

import io.github.codeladyship.hellofresh.dto.PageResponse;
import io.github.codeladyship.hellofresh.dto.PlanResponse;
import io.github.codeladyship.hellofresh.service.PlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/plans")
@Tag(name = "Plans", description = "Flexible plans: people x meals per week -> price")
public class PlanController {

    private final PlanService service;

    public PlanController(PlanService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "List all plans with their weekly price", description = "Paginated")
    public PageResponse<PlanResponse> list(
            @ParameterObject
            @PageableDefault(size = 20)
            @SortDefault.SortDefaults({
                    @SortDefault(sort = "people", direction = Sort.Direction.ASC),
                    @SortDefault(sort = "mealsPerWeek", direction = Sort.Direction.ASC)
            }) Pageable pageable) {
        return service.list(pageable);
    }

    @GetMapping("/quote")
    @Operation(summary = "Price for a number of people and meals per week")
    public PlanResponse quote(@RequestParam @Min(1) @Max(10) int people,
                              @RequestParam @Min(1) @Max(7) int mealsPerWeek) {
        return service.quote(people, mealsPerWeek);
    }
}
