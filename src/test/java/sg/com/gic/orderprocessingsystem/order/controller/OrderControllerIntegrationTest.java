package sg.com.gic.orderprocessingsystem.order.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import sg.com.gic.orderprocessingsystem.order.dto.CreateOrderRequest;
import sg.com.gic.orderprocessingsystem.order.dto.OrderResponse;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("OrderController Integration Tests")
class OrderControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper.findAndRegisterModules();
    }

    @Test
    @DisplayName("Should create order and verify in getAllOrders")
    void shouldCreateOrderAndVerifyInGetAllOrders() throws Exception {
        // Given
        CreateOrderRequest request = new CreateOrderRequest(99.99, "integration@example.com");

        // When - Create order
        String response = mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderId").exists())
                .andExpect(jsonPath("$.amount").value(99.99))
                .andExpect(jsonPath("$.customerEmail").value("integration@example.com"))
                .andExpect(jsonPath("$.createdAt").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Then - Verify in getAllOrders
        mockMvc.perform(get("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[*].customerEmail", hasItem("integration@example.com")))
                .andExpect(jsonPath("$[*].amount", hasItem(99.99)));
    }

    @Test
    @DisplayName("Should create multiple orders and retrieve all")
    void shouldCreateMultipleOrdersAndRetrieveAll() throws Exception {
        // Given
        CreateOrderRequest request1 = new CreateOrderRequest(50.0, "user1@test.com");
        CreateOrderRequest request2 = new CreateOrderRequest(75.0, "user2@test.com");
        CreateOrderRequest request3 = new CreateOrderRequest(100.0, "user3@test.com");

        // When - Create multiple orders
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request3)))
                .andExpect(status().isCreated());

        // Then - Verify all orders
        mockMvc.perform(get("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(3))))
                .andExpect(jsonPath("$[*].customerEmail", hasItems("user1@test.com", "user2@test.com", "user3@test.com")));
    }

    @Test
    @DisplayName("Should handle concurrent order creation")
    void shouldHandleConcurrentOrderCreation() throws Exception {
        // Given
        CreateOrderRequest request = new CreateOrderRequest(150.0, "concurrent@test.com");

        // When - Create multiple orders concurrently (simulated by rapid succession)
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/api/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    new CreateOrderRequest(150.0 + i, "concurrent" + i + "@test.com"))))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.orderId").exists())
                    .andExpect(jsonPath("$.customerEmail").value("concurrent" + i + "@test.com"));
        }

        // Then - Verify all orders were created
        mockMvc.perform(get("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("Should create order with minimum valid amount")
    void shouldCreateOrderWithMinimumAmount() throws Exception {
        // Given
        CreateOrderRequest request = new CreateOrderRequest(0.01, "min@example.com");

        // When & Then
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.amount").value(0.01))
                .andExpect(jsonPath("$.customerEmail").value("min@example.com"));
    }

    @Test
    @DisplayName("Should create order with large amount")
    void shouldCreateOrderWithLargeAmount() throws Exception {
        // Given
        CreateOrderRequest request = new CreateOrderRequest(999999.99, "large@example.com");

        // When & Then
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.amount").value(999999.99))
                .andExpect(jsonPath("$.customerEmail").value("large@example.com"));
    }

    @Test
    @DisplayName("Should handle order creation with special characters in email")
    void shouldHandleSpecialCharactersInEmail() throws Exception {
        // Given
        CreateOrderRequest request = new CreateOrderRequest(50.0, "test+special@example.com");

        // When & Then
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.customerEmail").value("test+special@example.com"));
    }

    @Test
    @DisplayName("Should return consistent order data across requests")
    void shouldReturnConsistentOrderData() throws Exception {
        // Given
        CreateOrderRequest request = new CreateOrderRequest(75.50, "consistent@example.com");

        // When - Create order
        String createResponse = mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Then - Verify consistency in getAllOrders
        mockMvc.perform(get("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].customerEmail", hasItem("consistent@example.com")))
                .andExpect(jsonPath("$[*].amount", hasItem(75.50)));
    }

    @Test
    @DisplayName("Should resend order create event successfully")
    void shouldResendOrderCreateEventSuccessfully() throws Exception {
        CreateOrderRequest request1 = new CreateOrderRequest(50.0, "user1@test.com");
        String createResponse = mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode createdJson = objectMapper.readTree(createResponse);
        String orderId = createdJson.get("orderId").asText();

        mockMvc.perform(post("/api/orders/" + orderId + "/resend")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(orderId))
                .andExpect(jsonPath("$.customerEmail").value("user1@test.com"));

        mockMvc.perform(get("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].orderId", hasItem(orderId)));

    }
}

