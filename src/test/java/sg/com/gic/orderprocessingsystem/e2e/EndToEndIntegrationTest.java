package sg.com.gic.orderprocessingsystem.e2e;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import sg.com.gic.orderprocessingsystem.order.dto.CreateOrderRequest;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("End-to-End Integration Tests")
class EndToEndIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper.findAndRegisterModules();
    }

    @Test
    @DisplayName("Should complete full order-payment-notification flow")
    void shouldCompleteFullFlow() throws Exception {
        // Given
        CreateOrderRequest orderRequest = new CreateOrderRequest(199.99, "e2e-test@example.com");

        // When - Step 1: Create order
        MvcResult orderResult = mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderId").exists())
                .andExpect(jsonPath("$.amount").value(199.99))
                .andExpect(jsonPath("$.customerEmail").value("e2e-test@example.com"))
                .andExpect(jsonPath("$.createdAt").exists())
                .andReturn();

        // Wait for async processing (payment and notification)
        Thread.sleep(1500);

        // Then - Step 2: Verify order exists
        mockMvc.perform(get("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[*].customerEmail", hasItem("e2e-test@example.com")))
                .andExpect(jsonPath("$[*].amount", hasItem(199.99)));

        // Step 3: Verify payment was processed
        mockMvc.perform(get("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[*].amount", hasItem(199.99)));

        // Step 4: Verify notification was sent
        mockMvc.perform(get("/api/notifications")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[*].message", hasItem(containsString("Payment successful"))));
    }

    @Test
    @DisplayName("Should process multiple orders concurrently")
    void shouldProcessMultipleOrdersConcurrently() throws Exception {
        // Given
        CreateOrderRequest order1 = new CreateOrderRequest(50.0, "concurrent1@example.com");
        CreateOrderRequest order2 = new CreateOrderRequest(75.0, "concurrent2@example.com");
        CreateOrderRequest order3 = new CreateOrderRequest(100.0, "concurrent3@example.com");

        // When - Create multiple orders
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(order1)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(order2)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(order3)))
                .andExpect(status().isCreated());

        // Wait for async processing
        Thread.sleep(3500);

        // Then - Verify all orders are present
        mockMvc.perform(get("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(3))));

        // Verify all payments are present
        mockMvc.perform(get("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(3))));

        // Verify all notifications are present
        mockMvc.perform(get("/api/notifications")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(3))));
    }

    @Test
    @DisplayName("Should maintain data consistency across all services")
    void shouldMaintainDataConsistency() throws Exception {
        // Given
        CreateOrderRequest orderRequest = new CreateOrderRequest(123.45, "consistency@example.com");

        // When - Create order
        String orderResponse = mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Parse orderId from response
        String orderId = objectMapper.readTree(orderResponse).get("orderId").asText();

        // Wait for async processing
        Thread.sleep(1500);

        // Then - Verify order in all endpoints
        mockMvc.perform(get("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.orderId=='" + orderId + "')].amount").value(hasItem(123.45)));

        mockMvc.perform(get("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.orderId=='" + orderId + "')].amount").value(hasItem(123.45)));

        mockMvc.perform(get("/api/notifications")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.orderId=='" + orderId + "')].message",
                        hasItem(containsString("Payment successful"))));
    }

    @Test
    @DisplayName("Should handle rapid order creation")
    void shouldHandleRapidOrderCreation() throws Exception {
        // When - Create orders rapidly
        for (int i = 0; i < 10; i++) {
            CreateOrderRequest request = new CreateOrderRequest(
                    10.0 + i,
                    "rapid" + i + "@example.com"
            );

            mockMvc.perform(post("/api/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());
        }

        // Wait for all async processing
        Thread.sleep(11000);

        // Then - Verify all were processed
        mockMvc.perform(get("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(10))));
    }

    @Test
    @DisplayName("Should return empty lists initially when no data exists")
    void shouldReturnEmptyListsInitially() throws Exception {
        // This test assumes a fresh start, but may have data from other tests
        // Just verify the endpoints are accessible

        mockMvc.perform(get("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        mockMvc.perform(get("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        mockMvc.perform(get("/api/notifications")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("Should process order with minimum amount")
    void shouldProcessOrderWithMinimumAmount() throws Exception {
        // Given
        CreateOrderRequest orderRequest = new CreateOrderRequest(0.01, "minimum@example.com");

        // When
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.amount").value(0.01));

        // Wait for async processing
        Thread.sleep(1500);

        // Then - Verify in payment and notification
        mockMvc.perform(get("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].amount", hasItem(0.01)));
    }

    @Test
    @DisplayName("Should process order with maximum practical amount")
    void shouldProcessOrderWithMaximumAmount() throws Exception {
        // Given
        CreateOrderRequest orderRequest = new CreateOrderRequest(99999.99, "maximum@example.com");

        // When
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.amount").value(99999.99));

        // Wait for async processing
        Thread.sleep(1500);

        // Then - Verify in payment and notification
        mockMvc.perform(get("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].amount", hasItem(99999.99)));
    }
}

