package sg.com.gic.orderprocessingsystem.order.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import sg.com.gic.orderprocessingsystem.exception.GlobalExceptionHandler;
import sg.com.gic.orderprocessingsystem.order.domain.Order;
import sg.com.gic.orderprocessingsystem.order.dto.CreateOrderRequest;
import sg.com.gic.orderprocessingsystem.order.service.OrderService;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderController Unit Tests")
class OrderControllerTest {

    @Mock
    private OrderService orderService;

    @InjectMocks
    private OrderController orderController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(orderController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
    }

    @Test
    @DisplayName("Should create order successfully with valid request")
    void shouldCreateOrderSuccessfully() throws Exception {
        // Given
        CreateOrderRequest request = new CreateOrderRequest(100.0, "test@example.com");
        Order expectedOrder = new Order("order-123", 100.0, "test@example.com", LocalDateTime.now());

        when(orderService.createOrder(anyDouble(), anyString())).thenReturn(expectedOrder);

        // When & Then
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderId").value("order-123"))
                .andExpect(jsonPath("$.amount").value(100.0))
                .andExpect(jsonPath("$.customerEmail").value("test@example.com"))
                .andExpect(jsonPath("$.createdAt").exists());

        verify(orderService, times(1)).createOrder(100.0, "test@example.com");
    }

    @Test
    @DisplayName("Should create order with different valid amounts")
    void shouldCreateOrderWithDifferentAmounts() throws Exception {
        // Given
        CreateOrderRequest request = new CreateOrderRequest(250.50, "customer@example.com");
        Order expectedOrder = new Order("order-456", 250.50, "customer@example.com", LocalDateTime.now());

        when(orderService.createOrder(anyDouble(), anyString())).thenReturn(expectedOrder);

        // When & Then
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderId").value("order-456"))
                .andExpect(jsonPath("$.amount").value(250.50))
                .andExpect(jsonPath("$.customerEmail").value("customer@example.com"));

        verify(orderService, times(1)).createOrder(250.50, "customer@example.com");
    }

    @Test
    @DisplayName("Should handle service exception when creating order")
    void shouldHandleServiceException() throws Exception {
        // Given
        CreateOrderRequest request = new CreateOrderRequest(100.0, "test@example.com");

        when(orderService.createOrder(anyDouble(), anyString()))
                .thenThrow(new RuntimeException("Service error"));

        // When & Then
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());

        verify(orderService, times(1)).createOrder(100.0, "test@example.com");
    }

    @Test
    @DisplayName("Should get all orders successfully when orders exist")
    void shouldGetAllOrdersSuccessfully() throws Exception {
        // Given
        List<Order> orders = Arrays.asList(
                new Order("order-1", 100.0, "user1@example.com", LocalDateTime.now()),
                new Order("order-2", 200.0, "user2@example.com", LocalDateTime.now())
        );

        when(orderService.getAllOrders()).thenReturn(orders);

        // When & Then
        mockMvc.perform(get("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].orderId").value("order-1"))
                .andExpect(jsonPath("$[0].amount").value(100.0))
                .andExpect(jsonPath("$[0].customerEmail").value("user1@example.com"))
                .andExpect(jsonPath("$[1].orderId").value("order-2"))
                .andExpect(jsonPath("$[1].amount").value(200.0))
                .andExpect(jsonPath("$[1].customerEmail").value("user2@example.com"));

        verify(orderService, times(1)).getAllOrders();
    }

    @Test
    @DisplayName("Should return empty list when no orders exist")
    void shouldReturnEmptyListWhenNoOrders() throws Exception {
        // Given
        when(orderService.getAllOrders()).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        verify(orderService, times(1)).getAllOrders();
    }

    @Test
    @DisplayName("Should return single order in list")
    void shouldReturnSingleOrderInList() throws Exception {
        // Given
        List<Order> orders = Collections.singletonList(
                new Order("order-1", 150.0, "single@example.com", LocalDateTime.now())
        );

        when(orderService.getAllOrders()).thenReturn(orders);

        // When & Then
        mockMvc.perform(get("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].orderId").value("order-1"))
                .andExpect(jsonPath("$[0].amount").value(150.0));

        verify(orderService, times(1)).getAllOrders();
    }

    @Test
    @DisplayName("Should handle null pointer exception gracefully")
    void shouldHandleNullPointerException() throws Exception {
        // Given
        when(orderService.getAllOrders()).thenThrow(new NullPointerException("Null value encountered"));

        // When & Then
        mockMvc.perform(get("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.message").value("A required value was null"));

        verify(orderService, times(1)).getAllOrders();
    }

    @Test
    @DisplayName("Should handle illegal argument exception when creating order")
    void shouldHandleIllegalArgumentException() throws Exception {
        // Given
        CreateOrderRequest request = new CreateOrderRequest(-100.0, "test@example.com");

        when(orderService.createOrder(anyDouble(), anyString()))
                .thenThrow(new IllegalArgumentException("Amount cannot be negative"));

        // When & Then
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Amount cannot be negative"));

        verify(orderService, times(1)).createOrder(-100.0, "test@example.com");
    }

    @Test
    @DisplayName("Should resend order successfully when order exists")
    void shouldResendOrderSuccessfully() throws Exception {
        // Given
        String orderId = "order-789";
        Order expectedOrder = new Order(orderId, 300.0, "resend@example.com", LocalDateTime.now());

        when(orderService.resendOrder(orderId)).thenReturn(expectedOrder);

        // When & Then
        mockMvc.perform(post("/api/orders/{orderId}/resend",orderId)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.orderId").value(orderId))
            .andExpect(jsonPath("$.amount").value(300.0))
            .andExpect(jsonPath("$.customerEmail").value("resend@example.com"))
            .andExpect(jsonPath("$.createdAt").exists());

        verify(orderService, times(1)).resendOrder(orderId);
    }

    @Test
    @DisplayName("Should return bad request when resending non-existent order")
    void shouldReturnBadRequestWhenResendingNonExistentOrder() throws Exception {
        // Given
        String orderId = "non-existent-order";

        when(orderService.resendOrder(anyString()))
            .thenThrow(new IllegalArgumentException("Order not found: " + orderId));

        // When & Then
        mockMvc.perform(post("/api/orders/{orderId}/resend",orderId)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.message").value("Order not found: " + orderId));

        verify(orderService, times(1)).resendOrder(orderId);
    }
}

