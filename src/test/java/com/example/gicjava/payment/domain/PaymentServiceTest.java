package com.example.gicjava.payment.domain;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import com.example.gicjava.eventBus.common.EventPublisher;
import com.example.gicjava.eventBus.common.EventSubscriber;
import com.example.gicjava.eventBus.common.event.OrderCreatedEvent;
import com.example.gicjava.eventBus.common.event.PaymentSucceededEvent;
import com.example.gicjava.payment.payment.domain.Payment;
import com.example.gicjava.payment.payment.domain.PaymentService;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

  @Mock
  private EventPublisher eventPublisher;

  @Mock
  private EventSubscriber eventSubscriber;

  @InjectMocks
  private PaymentService paymentService;

  @Test
  void handleOrderCreated_shouldProcessPaymentAndPublishEvent() {
    // Arrange
    OrderCreatedEvent event = new OrderCreatedEvent("order-1", 99.99, "c@example.com");

    // Act
    paymentService.handleOrderCreated(event);

    // Assert
    // Verify that a PaymentSucceededEvent was published (synchronous API)
    verify(eventPublisher).publish(any(PaymentSucceededEvent.class));

    // Payments list should contain at least one payment
    List<Payment> payments = paymentService.getAllPayments();
    Assertions.assertFalse(payments.isEmpty(), "Payments list should not be empty after processing an order");

    Payment p = payments.get(0);
    Assertions.assertEquals("order-1", p.orderId());
    Assertions.assertEquals(99.99, p.amount());
    Assertions.assertNotNull(p.paymentId());
    Assertions.assertFalse(p.paymentId().isEmpty());
    Assertions.assertNotNull(p.timestamp());
  }
}
