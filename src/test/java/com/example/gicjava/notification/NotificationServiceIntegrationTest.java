package com.example.gicjava.notification;

import com.example.gicjava.GicJavaApplication;
import com.example.gicjava.eventBus.common.EventPublisher;
import com.example.gicjava.eventBus.common.EventSubscriber;
import com.example.gicjava.eventBus.common.event.PaymentSucceededEvent;
import com.example.gicjava.notification.notification.domain.Notification;
import com.example.gicjava.notification.notification.domain.NotificationService;
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

// Disable springdoc during tests
@SpringBootTest(classes = GicJavaApplication.class)
@EnableAsync
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class NotificationServiceIntegrationTest {

  @Autowired
  private EventPublisher eventPublisher;

  @Autowired
  private EventSubscriber eventSubscriber;

  @Autowired
  private NotificationService notificationService;

  @Test
  void whenPaymentSucceeded_thenNotificationCreated() throws Exception {
    // Arrange
    CountDownLatch latch = new CountDownLatch(1);
    AtomicReference<PaymentSucceededEvent> captured = new AtomicReference<>();

    eventSubscriber.subscribe(PaymentSucceededEvent.class, (PaymentSucceededEvent ev) -> {
      captured.set(ev);
      latch.countDown();
    });

    PaymentSucceededEvent ev = new PaymentSucceededEvent("order-int-1", "pay-int-1", 77.77, java.time.LocalDateTime.now());

    // Act
    eventPublisher.publish(ev);

    // Wait for async handling
    boolean ok = latch.await(6, TimeUnit.SECONDS);
    Assertions.assertTrue(ok, "Timed out waiting for PaymentSucceededEvent handling");

    // Assert notification stored
    List<Notification> list = notificationService.getAllNotifications();
    Assertions.assertFalse(list.isEmpty(), "Notification list should not be empty");

    Notification n = list.get(0);
    Assertions.assertEquals("order-int-1", n.orderId());
    Assertions.assertEquals("pay-int-1", n.paymentId());
    Assertions.assertTrue(n.message().contains("Payment successful!"));
    Assertions.assertNotNull(n.notificationId());
    Assertions.assertNotNull(n.timestamp());
  }
}
