package io.github.codeladyship.hellofresh;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Full application (H2 + Flyway) end to end through MockMvc. */
@SpringBootTest
@AutoConfigureMockMvc
class ApiIntegrationTest {

    private static final String PAGES_ORIGIN = "https://codelady-ship.github.io";

    @Autowired
    private MockMvc mvc;

    @Test
    void servesSeededMealKitsInPageOrder() throws Exception {
        mvc.perform(get("/api/meal-kits"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(4))
                .andExpect(jsonPath("$.content[0].title").value("Meat & Veggies"))
                .andExpect(jsonPath("$.content[3].title").value("Fit & Wholesome"));
    }

    @Test
    void paginatesMealKits() throws Exception {
        mvc.perform(get("/api/meal-kits").param("size", "3").param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.last").value(true));
    }

    @Test
    void unknownSortPropertyIs400() throws Exception {
        mvc.perform(get("/api/meal-kits").param("sort", "doesNotExist"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid sort property"));
    }

    @Test
    void createsPlanRequestEndToEnd() throws Exception {
        mvc.perform(post("/api/plan-requests")
                        .header(HttpHeaders.ORIGIN, PAGES_ORIGIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Ana","email":"ana@example.com","people":4,"mealsPerWeek":4,"mealKitSlug":"family-friendly"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, PAGES_ORIGIN))
                .andExpect(jsonPath("$.weeklyTotal").value(138.83))
                .andExpect(jsonPath("$.mealKitSlug").value("family-friendly"));
    }

    @Test
    void corsPreflightAllowsGithubPagesAndLocalhost() throws Exception {
        for (String origin : new String[] {PAGES_ORIGIN, "http://localhost:5500", "http://127.0.0.1:3000"}) {
            mvc.perform(options("/api/plan-requests")
                            .header(HttpHeaders.ORIGIN, origin)
                            .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST")
                            .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "Content-Type"))
                    .andExpect(status().isOk())
                    .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, origin));
        }
    }

    @Test
    void corsRejectsOtherOrigins() throws Exception {
        mvc.perform(options("/api/plan-requests")
                        .header(HttpHeaders.ORIGIN, "https://evil.example.com")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST"))
                .andExpect(status().isForbidden());
    }

    @Test
    void healthIsUp() throws Exception {
        mvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void openApiDocsAreServed() throws Exception {
        mvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/meal-kits']").exists())
                .andExpect(jsonPath("$.paths['/api/plan-requests']").exists());
    }
}
