package io.github.codeladyship.hellofresh.controller;

import io.github.codeladyship.hellofresh.dto.MealKitResponse;
import io.github.codeladyship.hellofresh.dto.PageResponse;
import io.github.codeladyship.hellofresh.service.MealKitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/meal-kits")
@Tag(name = "Meal kits", description = "Recipes / meal kit categories shown in the menu carousel")
public class MealKitController {

    private final MealKitService service;

    public MealKitController(MealKitService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "List meal kits", description = "Paginated. Optional case-insensitive tag filter, e.g. ?tag=vegetarian")
    public PageResponse<MealKitResponse> list(
            @Parameter(description = "Only meal kits with this tag")
            @RequestParam(required = false) @Size(max = 40) String tag,
            @ParameterObject @PageableDefault(size = 12, sort = "sortOrder", direction = Sort.Direction.ASC) Pageable pageable) {
        return service.list(tag, pageable);
    }

    @GetMapping("/{slug}")
    @Operation(summary = "Get one meal kit by slug")
    public MealKitResponse get(@PathVariable @Pattern(regexp = "^[a-z0-9-]{1,80}$") String slug) {
        return service.getBySlug(slug);
    }
}
