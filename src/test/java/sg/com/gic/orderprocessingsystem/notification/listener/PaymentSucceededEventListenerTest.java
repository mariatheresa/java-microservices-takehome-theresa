package sg.com.gic.orderprocessingsystem.notification.listener;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sg.com.gic.orderprocessingsystem.eventbus.EventSubscriber;
import sg.com.gic.orderprocessingsystem.eventbus.event.PaymentSucceededEvent;
import sg.com.gic.orderprocessingsystem.notification.service.NotificationService;

import java.time.LocalDateTime;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentSucceededEventListener Unit Tests")
class PaymentSucceededEventListenerTest {

    @Mock
    private EventSubscriber eventSubscriber;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private PaymentSucceededEventListener listener;

    @Captor
    private ArgumentCaptor<Consumer<PaymentSucceededEvent>> handlerCaptor;

    @BeforeEach
    void setUp() {
        // Reset is handled by @ExtendWith(MockitoExtension.class)
    }

    @Test
    @Disabled("EventSubscriber removed - using Spring @EventListener now")
    @DisplayName("Should subscribe to PaymentSucceededEvent on initialization")
    void shouldSubscribeOnInit() {
        // Then
        verify(eventSubscriber, times(1)).subscribe(
                eq(PaymentSucceededEvent.class),
                any()
        );
    }

    @Test
    @DisplayName("Should handle PaymentSucceededEvent correctly")
    void shouldHandlePaymentSucceededEvent() {
        // Given
        PaymentSucceededEvent event = new PaymentSucceededEvent(
                "order-123", "pay-456", 100.0, LocalDateTime.now()
        );

        // When
        listener.handlePaymentSucceeded(event);

        // Then
        verify(notificationService, times(1)).processPaymentSucceededEvent(event);
    }

    @Test
    @DisplayName("Should pass event to notification service with correct data")
    void shouldPassEventToNotificationService() {
        // Given
        LocalDateTime timestamp = LocalDateTime.now();
        PaymentSucceededEvent event = new PaymentSucceededEvent(
                "order-789", "pay-101", 250.50, timestamp
        );

        // When
        listener.handlePaymentSucceeded(event);

        // Then
        ArgumentCaptor<PaymentSucceededEvent> eventCaptor = ArgumentCaptor.forClass(PaymentSucceededEvent.class);
        verify(notificationService, times(1)).processPaymentSucceededEvent(eventCaptor.capture());

        PaymentSucceededEvent capturedEvent = eventCaptor.getValue();
        assertThat(capturedEvent.orderId()).isEqualTo("order-789");
        assertThat(capturedEvent.paymentId()).isEqualTo("pay-101");
        assertThat(capturedEvent.amount()).isEqualTo(250.50);
        assertThat(capturedEvent.timestamp()).isEqualTo(timestamp);
    }

    @Test
    @DisplayName("Should handle multiple events sequentially")
    void shouldHandleMultipleEvents() {
        // Given
        PaymentSucceededEvent event1 = new PaymentSucceededEvent(
                "order-1", "pay-1", 100.0, LocalDateTime.now()
        );
        PaymentSucceededEvent event2 = new PaymentSucceededEvent(
                "order-2", "pay-2", 200.0, LocalDateTime.now()
        );
        PaymentSucceededEvent event3 = new PaymentSucceededEvent(
                "order-3", "pay-3", 300.0, LocalDateTime.now()
        );

        // When
        listener.handlePaymentSucceeded(event1);
        listener.handlePaymentSucceeded(event2);
        listener.handlePaymentSucceeded(event3);

        // Then
        verify(notificationService, times(3)).processPaymentSucceededEvent(any(PaymentSucceededEvent.class));
    }

    @Test
    @DisplayName("Should propagate notification service exception")
    void shouldPropagateNotificationServiceException() {
        // Given
        PaymentSucceededEvent event = new PaymentSucceededEvent(
                "order-error", "pay-error", 100.0, LocalDateTime.now()
        );
        doThrow(new RuntimeException("Notification processing failed"))
                .when(notificationService).processPaymentSucceededEvent(any(PaymentSucceededEvent.class));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            listener.handlePaymentSucceeded(event);
        });
    }

    @Test
    @Disabled("EventSubscriber removed - using Spring @EventListener now")
    @DisplayName("Should subscribe with correct event type")
    void shouldSubscribeWithCorrectEventType() {
        // Then
        verify(eventSubscriber).subscribe(
                eq(PaymentSucceededEvent.class),
                handlerCaptor.capture()
        );

        assertNotNull(handlerCaptor.getValue());
    }

    @Test
    @DisplayName("Should handle event with zero amount")
    void shouldHandleEventWithZeroAmount() {
        // Given
        PaymentSucceededEvent event = new PaymentSucceededEvent(
                "order-zero", "pay-zero", 0.0, LocalDateTime.now()
        );

        // When
        listener.handlePaymentSucceeded(event);

        // Then
        verify(notificationService, times(1)).processPaymentSucceededEvent(event);
    }

    @Test
    @DisplayName("Should handle event with large amount")
    void shouldHandleEventWithLargeAmount() {
        // Given
        PaymentSucceededEvent event = new PaymentSucceededEvent(
                "order-large", "pay-large", 999999.99, LocalDateTime.now()
        );

        // When
        listener.handlePaymentSucceeded(event);

        // Then
        verify(notificationService, times(1)).processPaymentSucceededEvent(event);
    }

    @Test
    @DisplayName("Should handle event with past timestamp")
    void shouldHandleEventWithPastTimestamp() {
        // Given
        LocalDateTime pastTime = LocalDateTime.now().minusDays(1);
        PaymentSucceededEvent event = new PaymentSucceededEvent(
                "order-past", "pay-past", 100.0, pastTime
        );

        // When
        listener.handlePaymentSucceeded(event);

        // Then
        verify(notificationService, times(1)).processPaymentSucceededEvent(event);
    }
}

