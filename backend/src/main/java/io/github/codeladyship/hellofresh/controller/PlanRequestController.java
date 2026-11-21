package io.github.codeladyship.hellofresh.controller;

import io.github.codeladyship.hellofresh.dto.PlanRequestCreate;
import io.github.codeladyship.hellofresh.dto.PlanRequestResponse;
import io.github.codeladyship.hellofresh.service.PlanRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/plan-requests")
@Tag(name = "Plan requests", description = "Requests sent from the Flexible Plans form")
public class PlanRequestController {

    private final PlanRequestService service;

    public PlanRequestController(PlanRequestService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Send a plan request", description = "Validates the form, prices the plan and stores the request")
    public PlanRequestResponse create(@Valid @RequestBody PlanRequestCreate body) {
        return service.create(body);
    }
}
