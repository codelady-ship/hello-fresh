package io.github.codeladyship.hellofresh.controller;

import io.github.codeladyship.hellofresh.dto.PlanRequestCreate;
import io.github.codeladyship.hellofresh.dto.PlanRequestResponse;
import io.github.codeladyship.hellofresh.entity.PlanRequestStatus;
import io.github.codeladyship.hellofresh.exception.InvalidRequestException;
import io.github.codeladyship.hellofresh.service.PlanRequestService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;

import static org.hamcrest.Matchers.hasItems;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PlanRequestController.class)
class PlanRequestControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private PlanRequestService service;

    @Test
    void createsPlanRequest() throws Exception {
        when(service.create(any(PlanRequestCreate.class))).thenReturn(new PlanRequestResponse(7L,
                PlanRequestStatus.RECEIVED, "Ana", 2, 3, "veggie", new BigDecimal("70.93"),
                Instant.parse("2026-11-18T10:00:00Z")));

        mvc.perform(post("/api/plan-requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Ana","email":"ana@example.com","people":2,"mealsPerWeek":3,"mealKitSlug":"veggie"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.status").value("RECEIVED"))
                .andExpect(jsonPath("$.weeklyTotal").value(70.93))
                .andExpect(jsonPath("$.email").doesNotExist());
    }

    @Test
    void invalidBodyReturnsFieldErrors() throws Exception {
        mvc.perform(post("/api/plan-requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"","email":"not-an-email","people":2}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.errors[*].field", hasItems("name", "email", "mealsPerWeek")));
        verifyNoInteractions(service);
    }

    @Test
    void malformedJsonIs400() throws Exception {
        mvc.perform(post("/api/plan-requests").contentType(MediaType.APPLICATION_JSON).content("{oops"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
    }

    @Test
    void unavailablePlanIs422() throws Exception {
        when(service.create(any(PlanRequestCreate.class)))
                .thenThrow(new InvalidRequestException("mealsPerWeek", "We do not offer 7 meals per week for 2 people yet"));

        mvc.perform(post("/api/plan-requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Ana","email":"ana@example.com","people":2,"mealsPerWeek":7}
                                """))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.errors[0].field").value("mealsPerWeek"));
    }
}
