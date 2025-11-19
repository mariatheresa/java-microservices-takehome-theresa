package sg.com.gic.orderprocessingsystem.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("GlobalExceptionHandler Integration Tests")
class GlobalExceptionHandlerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Should handle nonexistent endpoints")
    void shouldHandleNonexistentEndpoint() throws Exception {
        // When & Then - Spring Boot may return 404 or forward to error handler
        mockMvc.perform(get("/api/nonexistent")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("Should have valid endpoints for orders, payments, and notifications")
    void shouldHaveValidEndpoints() throws Exception {
        // When & Then - Verify main endpoints exist
        mockMvc.perform(get("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/notifications")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should return JSON content type for API responses")
    void shouldReturnJsonContentType() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }
}

