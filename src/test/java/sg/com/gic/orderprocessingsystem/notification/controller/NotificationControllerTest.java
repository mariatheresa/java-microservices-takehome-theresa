package sg.com.gic.orderprocessingsystem.notification.controller;

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
import sg.com.gic.orderprocessingsystem.notification.domain.Notification;
import sg.com.gic.orderprocessingsystem.notification.service.NotificationService;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationController Unit Tests")
class NotificationControllerTest {

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private NotificationController notificationController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(notificationController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
    }

    @Test
    @DisplayName("Should get all notifications successfully when notifications exist")
    void shouldGetAllNotificationsSuccessfully() throws Exception {
        // Given
        List<Notification> notifications = Arrays.asList(
                new Notification("notif-1", "order-1", "pay-1", "Payment successful", LocalDateTime.now()),
                new Notification("notif-2", "order-2", "pay-2", "Payment completed", LocalDateTime.now())
        );

        when(notificationService.getAllNotifications()).thenReturn(notifications);

        // When & Then
        mockMvc.perform(get("/api/notifications")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].notificationId").value("notif-1"))
                .andExpect(jsonPath("$[0].orderId").value("order-1"))
                .andExpect(jsonPath("$[0].paymentId").value("pay-1"))
                .andExpect(jsonPath("$[0].message").value("Payment successful"))
                .andExpect(jsonPath("$[1].notificationId").value("notif-2"))
                .andExpect(jsonPath("$[1].orderId").value("order-2"));

        verify(notificationService, times(1)).getAllNotifications();
    }

    @Test
    @DisplayName("Should return empty list when no notifications exist")
    void shouldReturnEmptyListWhenNoNotifications() throws Exception {
        // Given
        when(notificationService.getAllNotifications()).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/api/notifications")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        verify(notificationService, times(1)).getAllNotifications();
    }

    @Test
    @DisplayName("Should return single notification in list")
    void shouldReturnSingleNotificationInList() throws Exception {
        // Given
        List<Notification> notifications = Collections.singletonList(
                new Notification("notif-single", "order-single", "pay-single",
                        "Payment processed successfully", LocalDateTime.now())
        );

        when(notificationService.getAllNotifications()).thenReturn(notifications);

        // When & Then
        mockMvc.perform(get("/api/notifications")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].notificationId").value("notif-single"))
                .andExpect(jsonPath("$[0].message").value("Payment processed successfully"));

        verify(notificationService, times(1)).getAllNotifications();
    }

    @Test
    @DisplayName("Should handle service exception when getting notifications")
    void shouldHandleServiceException() throws Exception {
        // Given
        when(notificationService.getAllNotifications()).thenThrow(new RuntimeException("Service error"));

        // When & Then
        mockMvc.perform(get("/api/notifications")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Internal Server Error"));

        verify(notificationService, times(1)).getAllNotifications();
    }

    @Test
    @DisplayName("Should handle null pointer exception gracefully")
    void shouldHandleNullPointerException() throws Exception {
        // Given
        when(notificationService.getAllNotifications()).thenThrow(new NullPointerException("Null value"));

        // When & Then
        mockMvc.perform(get("/api/notifications")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.message").value("A required value was null"));

        verify(notificationService, times(1)).getAllNotifications();
    }

    @Test
    @DisplayName("Should return notifications with all required fields")
    void shouldReturnNotificationsWithAllFields() throws Exception {
        // Given
        LocalDateTime timestamp = LocalDateTime.now();
        List<Notification> notifications = Collections.singletonList(
                new Notification("notif-123", "order-456", "pay-789",
                        "Payment successful! Order order-456 has been paid", timestamp)
        );

        when(notificationService.getAllNotifications()).thenReturn(notifications);

        // When & Then
        mockMvc.perform(get("/api/notifications")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].notificationId").exists())
                .andExpect(jsonPath("$[0].orderId").exists())
                .andExpect(jsonPath("$[0].paymentId").exists())
                .andExpect(jsonPath("$[0].message").exists())
                .andExpect(jsonPath("$[0].timestamp").exists());

        verify(notificationService, times(1)).getAllNotifications();
    }

    @Test
    @DisplayName("Should return multiple notifications correctly")
    void shouldReturnMultipleNotifications() throws Exception {
        // Given
        List<Notification> notifications = Arrays.asList(
                new Notification("notif-1", "order-1", "pay-1", "Message 1", LocalDateTime.now()),
                new Notification("notif-2", "order-2", "pay-2", "Message 2", LocalDateTime.now()),
                new Notification("notif-3", "order-3", "pay-3", "Message 3", LocalDateTime.now()),
                new Notification("notif-4", "order-4", "pay-4", "Message 4", LocalDateTime.now())
        );

        when(notificationService.getAllNotifications()).thenReturn(notifications);

        // When & Then
        mockMvc.perform(get("/api/notifications")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(4))
                .andExpect(jsonPath("$[0].message").value("Message 1"))
                .andExpect(jsonPath("$[1].message").value("Message 2"))
                .andExpect(jsonPath("$[2].message").value("Message 3"))
                .andExpect(jsonPath("$[3].message").value("Message 4"));

        verify(notificationService, times(1)).getAllNotifications();
    }

    @Test
    @DisplayName("Should return notifications with long messages")
    void shouldReturnNotificationsWithLongMessages() throws Exception {
        // Given
        String longMessage = "Payment successful! Order order-123 has been paid with payment ID pay-456. " +
                "Amount: $1000.00. Thank you for your purchase.";
        List<Notification> notifications = Collections.singletonList(
                new Notification("notif-long", "order-123", "pay-456", longMessage, LocalDateTime.now())
        );

        when(notificationService.getAllNotifications()).thenReturn(notifications);

        // When & Then
        mockMvc.perform(get("/api/notifications")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].message").value(longMessage));

        verify(notificationService, times(1)).getAllNotifications();
    }
}

