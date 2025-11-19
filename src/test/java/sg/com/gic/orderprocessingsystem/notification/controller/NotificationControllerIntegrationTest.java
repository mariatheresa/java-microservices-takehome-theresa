package sg.com.gic.orderprocessingsystem.notification.controller;

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

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("NotificationController Integration Tests")
class NotificationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper.findAndRegisterModules();
    }

    @Test
    @DisplayName("Should create notification after order and payment processing")
    void shouldCreateNotificationAfterOrderAndPayment() throws Exception {
        // Given
        CreateOrderRequest orderRequest = new CreateOrderRequest(125.0, "notification-test@example.com");

        // When - Create order (triggers payment and notification)
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isCreated());

        // Wait for async processing (payment + notification)
        Thread.sleep(1500);

        // Then - Verify notification was created
        mockMvc.perform(get("/api/notifications")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[*].message", hasItem(containsString("Payment successful"))));
    }

    @Test
    @DisplayName("Should create notifications for multiple orders")
    void shouldCreateNotificationsForMultipleOrders() throws Exception {
        // Given
        CreateOrderRequest order1 = new CreateOrderRequest(50.0, "notif1@example.com");
        CreateOrderRequest order2 = new CreateOrderRequest(75.0, "notif2@example.com");

        // When - Create multiple orders
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(order1)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(order2)))
                .andExpect(status().isCreated());

        // Wait for async processing
        Thread.sleep(2500);

        // Then - Verify notifications were created
        mockMvc.perform(get("/api/notifications")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(2))));
    }

    @Test
    @DisplayName("Should return notifications with all required fields")
    void shouldReturnNotificationsWithAllFields() throws Exception {
        // Given
        CreateOrderRequest orderRequest = new CreateOrderRequest(99.99, "fields-notif@example.com");

        // When - Create order
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isCreated());

        // Wait for async processing
        Thread.sleep(1500);

        // Then - Verify notification has all fields
        mockMvc.perform(get("/api/notifications")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].notificationId").exists())
                .andExpect(jsonPath("$[0].orderId").exists())
                .andExpect(jsonPath("$[0].paymentId").exists())
                .andExpect(jsonPath("$[0].message").exists())
                .andExpect(jsonPath("$[0].timestamp").exists());
    }

    @Test
    @DisplayName("Should retrieve notifications consistently across multiple requests")
    void shouldRetrieveNotificationsConsistently() throws Exception {
        // Given
        CreateOrderRequest orderRequest = new CreateOrderRequest(88.88, "consistent-notif@example.com");

        // When - Create order
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isCreated());

        // Wait for async processing
        Thread.sleep(1500);

        // Then - Verify consistency across multiple GET requests
        String response1 = mockMvc.perform(get("/api/notifications")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String response2 = mockMvc.perform(get("/api/notifications")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Responses should be identical
        assert response1.equals(response2);
    }

    @Test
    @DisplayName("Should include order ID and payment ID in notification message")
    void shouldIncludeOrderAndPaymentIdInMessage() throws Exception {
        // Given
        CreateOrderRequest orderRequest = new CreateOrderRequest(150.0, "message-test@example.com");

        // When - Create order and get the order ID
        String orderResponse = mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Wait for async processing
        Thread.sleep(1500);

        // Then - Verify notification message contains relevant IDs
        mockMvc.perform(get("/api/notifications")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].message", hasItem(containsString("Order"))))
                .andExpect(jsonPath("$[*].message", hasItem(containsString("payment ID"))));
    }
}

