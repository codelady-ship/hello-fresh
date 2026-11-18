package io.github.codeladyship.hellofresh.controller;

import io.github.codeladyship.hellofresh.dto.PageResponse;
import io.github.codeladyship.hellofresh.dto.PlanResponse;
import io.github.codeladyship.hellofresh.exception.NotFoundException;
import io.github.codeladyship.hellofresh.service.PlanService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PlanController.class)
class PlanControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private PlanService service;

    private static final PlanResponse TWO_BY_THREE = new PlanResponse(1L, 2, 3, 6,
            new BigDecimal("9.99"), new BigDecimal("10.99"), new BigDecimal("70.93"));

    @Test
    void listsPlans() throws Exception {
        when(service.list(any(Pageable.class))).thenReturn(new PageResponse<>(List.of(TWO_BY_THREE), 0, 20, 1, 1, true));

        mvc.perform(get("/api/plans"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].people").value(2))
                .andExpect(jsonPath("$.content[0].weeklyTotal").value(70.93));
    }

    @Test
    void quotesAPlan() throws Exception {
        when(service.quote(2, 3)).thenReturn(TWO_BY_THREE);

        mvc.perform(get("/api/plans/quote").param("people", "2").param("mealsPerWeek", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.servingsPerWeek").value(6))
                .andExpect(jsonPath("$.pricePerServing").value(9.99));
    }

    @Test
    void quoteForUnknownCombinationIs404() throws Exception {
        when(service.quote(3, 3)).thenThrow(new NotFoundException("No plan for 3 people and 3 meals per week"));

        mvc.perform(get("/api/plans/quote").param("people", "3").param("mealsPerWeek", "3"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
    }

    @Test
    void quoteOutOfRangeIs400() throws Exception {
        mvc.perform(get("/api/plans/quote").param("people", "0").param("mealsPerWeek", "9"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.errors.length()").value(2));
        verifyNoInteractions(service);
    }

    @Test
    void quoteWithMissingParameterIs400() throws Exception {
        mvc.perform(get("/api/plans/quote").param("people", "2"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
    }
}
