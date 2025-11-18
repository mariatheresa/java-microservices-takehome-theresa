package com.example.gicjava.payment;

import com.example.gicjava.GicJavaApplication;
import com.example.gicjava.eventBus.common.EventPublisher;
import com.example.gicjava.eventBus.common.EventSubscriber;
import com.example.gicjava.eventBus.common.event.OrderCreatedEvent;
import com.example.gicjava.eventBus.common.event.PaymentSucceededEvent;
import com.example.gicjava.payment.payment.domain.Payment;
import com.example.gicjava.payment.payment.domain.PaymentService;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest(classes = GicJavaApplication.class)
@EnableAsync
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class PaymentServiceIntegrationTest {

  @Autowired
  private EventPublisher eventPublisher;

  @Autowired
  private EventSubscriber eventSubscriber;

  @Autowired
  private PaymentService paymentService;

  @Test
  void whenOrderCreated_thenPaymentProcessedAndPaymentSucceededEventPublished() throws Exception {
    // Arrange
    CountDownLatch latch = new CountDownLatch(1);
    AtomicReference<PaymentSucceededEvent> captured = new AtomicReference<>();

    eventSubscriber.subscribe(PaymentSucceededEvent.class, (PaymentSucceededEvent ev) -> {
      captured.set(ev);
      latch.countDown();
    });

    OrderCreatedEvent orderEvent = new OrderCreatedEvent("order-int-1", 123.45, "i@example.com");

    // Act
    eventPublisher.publish(orderEvent);

    // Wait for async processing (PaymentService.handleOrderCreated has a 1s sleep)
    boolean completed = latch.await(6, TimeUnit.SECONDS);
    Assertions.assertTrue(completed, "Timed out waiting for PaymentSucceededEvent to be published");

    // Assert payment recorded
    List<Payment> payments = paymentService.getAllPayments();
    Assertions.assertFalse(payments.isEmpty(), "Payment list should not be empty");

    Payment p = payments.get(0);
    Assertions.assertEquals("order-int-1", p.orderId());
    Assertions.assertEquals(123.45, p.amount());
    Assertions.assertNotNull(p.paymentId());
    Assertions.assertFalse(p.paymentId().isEmpty());
    Assertions.assertNotNull(p.timestamp());

    // Assert published event
    PaymentSucceededEvent published = captured.get();
    Assertions.assertNotNull(published, "Expected a PaymentSucceededEvent to be captured");
    Assertions.assertEquals("order-int-1", published.orderId());
    Assertions.assertEquals(123.45, published.amount());
    Assertions.assertNotNull(published.paymentId());
    Assertions.assertFalse(published.paymentId().isEmpty());
  }
}
