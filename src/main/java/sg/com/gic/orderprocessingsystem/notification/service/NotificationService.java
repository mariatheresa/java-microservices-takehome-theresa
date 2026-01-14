package sg.com.gic.orderprocessingsystem.notification.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import sg.com.gic.orderprocessingsystem.eventbus.event.PaymentSucceededEvent;
import sg.com.gic.orderprocessingsystem.notification.domain.Notification;
import sg.com.gic.orderprocessingsystem.notification.entity.NotificationEntity;
import sg.com.gic.orderprocessingsystem.notification.repository.NotificationRepository;

@Service
public class NotificationService {

  private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);
  private final NotificationRepository notificationRepository;

  public NotificationService(NotificationRepository notificationRepository) {
    this.notificationRepository = notificationRepository;
  }

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
    NotificationEntity notificationEntity = new NotificationEntity(
        notification.notificationId(),
        notification.orderId(),
        notification.paymentId(),
        notification.message(),
        notification.timestamp());
    notificationRepository.save(notificationEntity);
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
    return notificationRepository.findAll()
        .stream()
        .map(e -> new Notification(
            e.getNotificationId(),
            e.getOrderId(),
            e.getPaymentId(),
            e.getMessage(),
            e.getTimestamp()
        )).collect(Collectors.toList());
  }
}
