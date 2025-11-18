package com.example.gicjava.notification.notification.domain;

import com.example.gicjava.eventBus.common.EventSubscriber;
import com.example.gicjava.eventBus.common.event.PaymentSucceededEvent;
import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

  private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);
  private final List<Notification> notifications = new ArrayList<>();
  private final EventSubscriber eventSubscriber;

  public NotificationService(EventSubscriber eventSubscriber) {
    this.eventSubscriber = eventSubscriber;
  }

  @PostConstruct
  public void init() {
    // Subscribe to PaymentSucceededEvent
    eventSubscriber.subscribe(PaymentSucceededEvent.class, this::handlePaymentSucceeded);
    logger.info("NotificationService subscribed to PaymentSucceededEvent");
  }

  public void handlePaymentSucceeded(PaymentSucceededEvent event) {
    logger.info("Received PaymentSucceededEvent for order: {}, payment: {}",
        event.orderId(), event.paymentId());

    try {
      // Generate notification ID
      String notificationId = UUID.randomUUID().toString();
      LocalDateTime timestamp = LocalDateTime.now();

      // Create notification message
      String message = String.format(
          "Payment successful! Order %s has been paid with payment ID %s. Amount: $%.2f",
          event.orderId(),
          event.paymentId(),
          event.amount());

      // Store notification
      Notification notification = new Notification(
          notificationId,
          event.orderId(),
          event.paymentId(),
          message,
          timestamp);
      notifications.add(notification);

      // Simulate sending notification (log to console)
      logger.info("📧 NOTIFICATION SENT: {}", message);
      logger.info("Notification details - ID: {}, Timestamp: {}", notificationId, timestamp);

    } catch (Exception e) {
      logger.error("Failed to send notification for order: {}", event.orderId(), e);
    }
  }

  public List<Notification> getAllNotifications() {
    return new ArrayList<>(notifications);
  }
}
