package sg.com.gic.orderprocessingsystem.payment.controller;

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
import sg.com.gic.orderprocessingsystem.payment.domain.Payment;
import sg.com.gic.orderprocessingsystem.payment.service.PaymentService;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentController Unit Tests")
class PaymentControllerTest {

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private PaymentController paymentController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(paymentController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
    }

    @Test
    @DisplayName("Should get all payments successfully when payments exist")
    void shouldGetAllPaymentsSuccessfully() throws Exception {
        // Given
        List<Payment> payments = Arrays.asList(
                new Payment("pay-1", "order-1", 100.0, LocalDateTime.now()),
                new Payment("pay-2", "order-2", 200.0, LocalDateTime.now())
        );

        when(paymentService.getAllPayments()).thenReturn(payments);

        // When & Then
        mockMvc.perform(get("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].paymentId").value("pay-1"))
                .andExpect(jsonPath("$[0].orderId").value("order-1"))
                .andExpect(jsonPath("$[0].amount").value(100.0))
                .andExpect(jsonPath("$[1].paymentId").value("pay-2"))
                .andExpect(jsonPath("$[1].orderId").value("order-2"))
                .andExpect(jsonPath("$[1].amount").value(200.0));

        verify(paymentService, times(1)).getAllPayments();
    }

    @Test
    @DisplayName("Should return empty list when no payments exist")
    void shouldReturnEmptyListWhenNoPayments() throws Exception {
        // Given
        when(paymentService.getAllPayments()).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        verify(paymentService, times(1)).getAllPayments();
    }

    @Test
    @DisplayName("Should return single payment in list")
    void shouldReturnSinglePaymentInList() throws Exception {
        // Given
        List<Payment> payments = Collections.singletonList(
                new Payment("pay-single", "order-single", 150.0, LocalDateTime.now())
        );

        when(paymentService.getAllPayments()).thenReturn(payments);

        // When & Then
        mockMvc.perform(get("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].paymentId").value("pay-single"))
                .andExpect(jsonPath("$[0].amount").value(150.0));

        verify(paymentService, times(1)).getAllPayments();
    }

    @Test
    @DisplayName("Should handle service exception when getting payments")
    void shouldHandleServiceException() throws Exception {
        // Given
        when(paymentService.getAllPayments()).thenThrow(new RuntimeException("Service error"));

        // When & Then
        mockMvc.perform(get("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Internal Server Error"));

        verify(paymentService, times(1)).getAllPayments();
    }

    @Test
    @DisplayName("Should handle null pointer exception gracefully")
    void shouldHandleNullPointerException() throws Exception {
        // Given
        when(paymentService.getAllPayments()).thenThrow(new NullPointerException("Null value"));

        // When & Then
        mockMvc.perform(get("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.message").value("A required value was null"));

        verify(paymentService, times(1)).getAllPayments();
    }

    @Test
    @DisplayName("Should return payments with all required fields")
    void shouldReturnPaymentsWithAllFields() throws Exception {
        // Given
        LocalDateTime timestamp = LocalDateTime.now();
        List<Payment> payments = Collections.singletonList(
                new Payment("pay-123", "order-456", 99.99, timestamp)
        );

        when(paymentService.getAllPayments()).thenReturn(payments);

        // When & Then
        mockMvc.perform(get("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].paymentId").exists())
                .andExpect(jsonPath("$[0].orderId").exists())
                .andExpect(jsonPath("$[0].amount").exists())
                .andExpect(jsonPath("$[0].timestamp").exists());

        verify(paymentService, times(1)).getAllPayments();
    }

    @Test
    @DisplayName("Should return multiple payments correctly")
    void shouldReturnMultiplePayments() throws Exception {
        // Given
        List<Payment> payments = Arrays.asList(
                new Payment("pay-1", "order-1", 50.0, LocalDateTime.now()),
                new Payment("pay-2", "order-2", 75.0, LocalDateTime.now()),
                new Payment("pay-3", "order-3", 100.0, LocalDateTime.now()),
                new Payment("pay-4", "order-4", 125.0, LocalDateTime.now())
        );

        when(paymentService.getAllPayments()).thenReturn(payments);

        // When & Then
        mockMvc.perform(get("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(4))
                .andExpect(jsonPath("$[0].amount").value(50.0))
                .andExpect(jsonPath("$[1].amount").value(75.0))
                .andExpect(jsonPath("$[2].amount").value(100.0))
                .andExpect(jsonPath("$[3].amount").value(125.0));

        verify(paymentService, times(1)).getAllPayments();
    }

    @Test
    @DisplayName("Should return payments with different amounts")
    void shouldReturnPaymentsWithDifferentAmounts() throws Exception {
        // Given
        List<Payment> payments = Arrays.asList(
                new Payment("pay-1", "order-1", 0.01, LocalDateTime.now()),
                new Payment("pay-2", "order-2", 999999.99, LocalDateTime.now())
        );

        when(paymentService.getAllPayments()).thenReturn(payments);

        // When & Then
        mockMvc.perform(get("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].amount").value(0.01))
                .andExpect(jsonPath("$[1].amount").value(999999.99));

        verify(paymentService, times(1)).getAllPayments();
    }
}

