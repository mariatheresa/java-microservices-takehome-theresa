package sg.com.gic.orderprocessingsystem.payment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import sg.com.gic.orderprocessingsystem.order.dto.CreateOrderRequest;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@DisplayName("PaymentController Integration Tests")
class PaymentControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper.findAndRegisterModules();
    }

    @Test
    @DisplayName("Should process payment after order creation and verify in getAllPayments")
    void shouldProcessPaymentAfterOrderCreation() throws Exception {
        // Given
        CreateOrderRequest orderRequest = new CreateOrderRequest(150.0, "payment-test@example.com");

        // When - Create order (which triggers payment processing)
        String orderResponse = mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Wait for async payment processing
        Thread.sleep(1500);

        // Then - Verify payment was processed
        mockMvc.perform(get("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[*].amount", hasItem(150.0)));
    }

    @Test
    @DisplayName("Should handle multiple payments from multiple orders")
    void shouldHandleMultiplePayments() throws Exception {
        // Given
        CreateOrderRequest order1 = new CreateOrderRequest(100.0, "multi1@example.com");
        CreateOrderRequest order2 = new CreateOrderRequest(200.0, "multi2@example.com");

        // When - Create multiple orders
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(order1)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(order2)))
                .andExpect(status().isCreated());

        // Wait for async payment processing
        Thread.sleep(2500);

        // Then - Verify payments were processed
        mockMvc.perform(get("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isNotEmpty())
                .andExpect(jsonPath("$[*].amount", hasItems(100.0, 200.0, 75.0)));
    }

    @Test
    @DisplayName("Should return payments with all required fields")
    void shouldReturnPaymentsWithAllFields() throws Exception {
        // Given
        CreateOrderRequest orderRequest = new CreateOrderRequest(99.99, "fields-test@example.com");

        // When - Create order
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isCreated());

        // Wait for async payment processing
        Thread.sleep(1500);

        // Then - Verify payment has all fields
        mockMvc.perform(get("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].paymentId").exists())
                .andExpect(jsonPath("$[0].orderId").exists())
                .andExpect(jsonPath("$[0].amount").exists())
                .andExpect(jsonPath("$[0].timestamp").exists());
    }

    @Test
    @DisplayName("Should retrieve payments consistently across multiple requests")
    void shouldRetrievePaymentsConsistently() throws Exception {
        // Given
        CreateOrderRequest orderRequest = new CreateOrderRequest(75.0, "consistent@example.com");

        // When - Create order
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isCreated());

        // Wait for async payment processing
        Thread.sleep(1500);

        // Then - Verify consistency across multiple GET requests
        String response1 = mockMvc.perform(get("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String response2 = mockMvc.perform(get("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Responses should be identical
        assert response1.equals(response2);
    }
}

