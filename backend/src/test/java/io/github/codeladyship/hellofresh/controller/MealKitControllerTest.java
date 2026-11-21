package io.github.codeladyship.hellofresh.controller;

import io.github.codeladyship.hellofresh.dto.MealKitResponse;
import io.github.codeladyship.hellofresh.dto.PageResponse;
import io.github.codeladyship.hellofresh.exception.NotFoundException;
import io.github.codeladyship.hellofresh.service.MealKitService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MealKitController.class)
class MealKitControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private MealKitService service;

    private static final MealKitResponse VEGGIE = new MealKitResponse(2L, "veggie", "Veggie", "& plant-based meals",
            "Meat-free recipes", "images/page4-2.jpg", 25, 580, new BigDecimal("8.99"),
            List.of("plant-based", "vegetarian"));

    @Test
    void listsMealKitsWithPaginationMetadata() throws Exception {
        when(service.list(isNull(), any(Pageable.class)))
                .thenReturn(new PageResponse<>(List.of(VEGGIE), 0, 12, 1, 1, true));

        mvc.perform(get("/api/meal-kits"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content[0].slug").value("veggie"))
                .andExpect(jsonPath("$.content[0].pricePerServing").value(8.99))
                .andExpect(jsonPath("$.content[0].tags[1]").value("vegetarian"))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.page").value(0));
    }

    @Test
    void passesTagAndPageRequestToService() throws Exception {
        when(service.list(eq("vegetarian"), any(Pageable.class)))
                .thenReturn(new PageResponse<>(List.of(VEGGIE), 1, 2, 3, 2, true));

        mvc.perform(get("/api/meal-kits").param("tag", "vegetarian").param("page", "1").param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(1));

        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(service).list(eq("vegetarian"), pageable.capture());
        assertThat(pageable.getValue().getPageNumber()).isEqualTo(1);
        assertThat(pageable.getValue().getPageSize()).isEqualTo(2);
    }

    @Test
    void returnsOneMealKitBySlug() throws Exception {
        when(service.getBySlug("veggie")).thenReturn(VEGGIE);

        mvc.perform(get("/api/meal-kits/veggie"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Veggie"))
                .andExpect(jsonPath("$.cookingTimeMinutes").value(25))
                .andExpect(jsonPath("$.calories").value(580));
    }

    @Test
    void unknownSlugIsProblemJson404() throws Exception {
        when(service.getBySlug("nope")).thenThrow(new NotFoundException("Meal kit 'nope' was not found"));

        mvc.perform(get("/api/meal-kits/nope"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("Meal kit 'nope' was not found"));
    }

    @Test
    void invalidSlugIsRejectedWith400() throws Exception {
        mvc.perform(get("/api/meal-kits/BAD_SLUG!"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.errors[0].field").value("slug"));
    }
}
