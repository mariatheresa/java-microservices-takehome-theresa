package sg.com.gic.orderprocessingsystem.notification.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import sg.com.gic.orderprocessingsystem.eventbus.event.PaymentSucceededEvent;
import sg.com.gic.orderprocessingsystem.notification.domain.Notification;

@Service
public class NotificationService {

  private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);
  private final List<Notification> notifications = new ArrayList<>();

  public void processPaymentSucceededEvent(PaymentSucceededEvent event) {
    Notification notification = createNotification(event);
    send(notification);
    save(notification);
    logger.info("Notification sent - ID: {}, Timestamp: {}", notification.notificationId(),
        notification.timestamp());
  }

  private void send(Notification notification) {
    logger.info("Sending notification: {}", notification.message());
  }

  private void save(Notification notification) {
    notifications.add(notification);
  }

  private Notification createNotification(PaymentSucceededEvent event) {
    String notificationId = UUID.randomUUID().toString();
    LocalDateTime timestamp = LocalDateTime.now();

    // Create notification message
    String message = String.format(
        "Payment successful! Order %s has been paid with payment ID %s. Amount: $%.2f",
        event.orderId(),
        event.paymentId(),
        event.amount());

    // Store notification
    return new Notification(
        notificationId,
        event.orderId(),
        event.paymentId(),
        message,
        timestamp);
  }

  public List<Notification> getAllNotifications() {
    return notifications;
  }
}
