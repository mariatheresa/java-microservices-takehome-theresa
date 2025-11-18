package com.example.gicjava.notification.domain;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import com.example.gicjava.eventBus.common.EventSubscriber;
import com.example.gicjava.eventBus.common.event.PaymentSucceededEvent;
import com.example.gicjava.notification.notification.domain.Notification;
import com.example.gicjava.notification.notification.domain.NotificationService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

  @Mock
  private EventSubscriber eventSubscriber;

  @InjectMocks
  private NotificationService notificationService;

  @Test
  void handlePaymentSucceeded_shouldCreateNotificationAndLog() {
    // Arrange
    PaymentSucceededEvent event = new PaymentSucceededEvent("order-1", "payment-1", 50.0, LocalDateTime.now());

    // Act
    // register subscription (simulate PostConstruct behavior)
    notificationService.init();

    notificationService.handlePaymentSucceeded(event);

    // Assert stored notification
    List<Notification> notifications = notificationService.getAllNotifications();
    Assertions.assertFalse(notifications.isEmpty(), "Notifications list should not be empty after handling event");

    Notification n = notifications.get(0);
    Assertions.assertEquals("order-1", n.orderId());
    Assertions.assertEquals("payment-1", n.paymentId());
    Assertions.assertTrue(n.message().contains("Payment successful!"));
    Assertions.assertNotNull(n.notificationId());
    Assertions.assertNotNull(n.timestamp());

    // verify subscriber registration during init
    verify(eventSubscriber).subscribe(eq(PaymentSucceededEvent.class), any());
  }
}
