package sg.com.gic.orderprocessingsystem.payment.listener;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sg.com.gic.orderprocessingsystem.eventbus.EventSubscriber;
import sg.com.gic.orderprocessingsystem.eventbus.event.OrderCreatedEvent;
import sg.com.gic.orderprocessingsystem.payment.service.PaymentService;

import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderCreatedEventListener Unit Tests")
class OrderCreatedEventListenerTest {

    @Mock
    private EventSubscriber eventSubscriber;

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private OrderCreatedEventListener listener;

    @Captor
    private ArgumentCaptor<Consumer<OrderCreatedEvent>> handlerCaptor;

    @BeforeEach
    void setUp() {
        // Reset is handled by @ExtendWith(MockitoExtension.class)
    }


    @Test
    @DisplayName("Should handle OrderCreatedEvent correctly")
    void shouldHandleOrderCreatedEvent() {
        // Given
        OrderCreatedEvent event = new OrderCreatedEvent("order-123", 100.0, "test@example.com");

        // When
        listener.handleOrderCreated(event);

        // Then
        verify(paymentService, times(1)).processOrderCreatedEvent(event);
    }

    @Test
    @DisplayName("Should pass event to payment service with correct data")
    void shouldPassEventToPaymentService() {
        // Given
        OrderCreatedEvent event = new OrderCreatedEvent("order-456", 250.50, "customer@example.com");

        // When
        listener.handleOrderCreated(event);

        // Then
        ArgumentCaptor<OrderCreatedEvent> eventCaptor = ArgumentCaptor.forClass(OrderCreatedEvent.class);
        verify(paymentService, times(1)).processOrderCreatedEvent(eventCaptor.capture());

        OrderCreatedEvent capturedEvent = eventCaptor.getValue();
        assertThat(capturedEvent.orderId()).isEqualTo("order-456");
        assertThat(capturedEvent.amount()).isEqualTo(250.50);
        assertThat(capturedEvent.customerEmail()).isEqualTo("customer@example.com");
    }

    @Test
    @DisplayName("Should handle multiple events sequentially")
    void shouldHandleMultipleEvents() {
        // Given
        OrderCreatedEvent event1 = new OrderCreatedEvent("order-1", 100.0, "test1@example.com");
        OrderCreatedEvent event2 = new OrderCreatedEvent("order-2", 200.0, "test2@example.com");
        OrderCreatedEvent event3 = new OrderCreatedEvent("order-3", 300.0, "test3@example.com");

        // When
        listener.handleOrderCreated(event1);
        listener.handleOrderCreated(event2);
        listener.handleOrderCreated(event3);

        // Then
        verify(paymentService, times(3)).processOrderCreatedEvent(any(OrderCreatedEvent.class));
    }

    @Test
    @DisplayName("Should propagate payment service exception")
    void shouldPropagatePaymentServiceException() {
        // Given
        OrderCreatedEvent event = new OrderCreatedEvent("order-error", 100.0, "error@example.com");
        doThrow(new RuntimeException("Payment processing failed"))
                .when(paymentService).processOrderCreatedEvent(any(OrderCreatedEvent.class));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            listener.handleOrderCreated(event);
        });
    }

    @Test
    @DisplayName("Should handle event with zero amount")
    void shouldHandleEventWithZeroAmount() {
        // Given
        OrderCreatedEvent event = new OrderCreatedEvent("order-zero", 0.0, "zero@example.com");

        // When
        listener.handleOrderCreated(event);

        // Then
        verify(paymentService, times(1)).processOrderCreatedEvent(event);
    }

    @Test
    @DisplayName("Should handle event with large amount")
    void shouldHandleEventWithLargeAmount() {
        // Given
        OrderCreatedEvent event = new OrderCreatedEvent("order-large", 999999.99, "large@example.com");

        // When
        listener.handleOrderCreated(event);

        // Then
        verify(paymentService, times(1)).processOrderCreatedEvent(event);
    }
}

