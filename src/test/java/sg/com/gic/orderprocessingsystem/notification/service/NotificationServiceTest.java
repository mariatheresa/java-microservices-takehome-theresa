package sg.com.gic.orderprocessingsystem.notification.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import sg.com.gic.orderprocessingsystem.eventbus.event.PaymentSucceededEvent;
import sg.com.gic.orderprocessingsystem.notification.domain.Notification;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationService Unit Tests")
class NotificationServiceTest {

    @InjectMocks
    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        // Reset is handled by @ExtendWith(MockitoExtension.class)
    }

    @Test
    @DisplayName("Should process PaymentSucceededEvent successfully")
    void shouldProcessPaymentSucceededEvent() {
        // Given
        PaymentSucceededEvent event = new PaymentSucceededEvent(
                "order-123", "pay-456", 100.0, LocalDateTime.now()
        );

        // When
        notificationService.processPaymentSucceededEvent(event);

        // Then
        List<Notification> notifications = notificationService.getAllNotifications();
        assertThat(notifications).hasSize(1);
        assertThat(notifications.get(0).orderId()).isEqualTo("order-123");
        assertThat(notifications.get(0).paymentId()).isEqualTo("pay-456");
        assertNotNull(notifications.get(0).notificationId());
        assertNotNull(notifications.get(0).message());
        assertNotNull(notifications.get(0).timestamp());
    }

    @Test
    @DisplayName("Should generate notification message with order and payment details")
    void shouldGenerateNotificationMessageWithDetails() {
        // Given
        PaymentSucceededEvent event = new PaymentSucceededEvent(
                "order-789", "pay-101", 250.50, LocalDateTime.now()
        );

        // When
        notificationService.processPaymentSucceededEvent(event);

        // Then
        List<Notification> notifications = notificationService.getAllNotifications();
        Notification notification = notifications.get(0);

        assertThat(notification.message()).contains("Payment successful");
        assertThat(notification.message()).contains("order-789");
        assertThat(notification.message()).contains("pay-101");
        assertThat(notification.message()).contains("250.50");
    }

    @Test
    @DisplayName("Should generate unique notification IDs for different events")
    void shouldGenerateUniqueNotificationIds() {
        // Given
        PaymentSucceededEvent event1 = new PaymentSucceededEvent(
                "order-1", "pay-1", 100.0, LocalDateTime.now()
        );
        PaymentSucceededEvent event2 = new PaymentSucceededEvent(
                "order-2", "pay-2", 200.0, LocalDateTime.now()
        );

        // When
        notificationService.processPaymentSucceededEvent(event1);
        notificationService.processPaymentSucceededEvent(event2);

        // Then
        List<Notification> notifications = notificationService.getAllNotifications();
        assertThat(notifications).hasSize(2);
        assertNotEquals(notifications.get(0).notificationId(), notifications.get(1).notificationId());
    }

    @Test
    @DisplayName("Should store notification in internal list")
    void shouldStoreNotificationInList() {
        // Given
        PaymentSucceededEvent event = new PaymentSucceededEvent(
                "order-store", "pay-store", 150.0, LocalDateTime.now()
        );

        // When
        notificationService.processPaymentSucceededEvent(event);
        List<Notification> notifications = notificationService.getAllNotifications();

        // Then
        assertThat(notifications).hasSize(1);
        Notification notification = notifications.get(0);
        assertThat(notification.orderId()).isEqualTo("order-store");
        assertThat(notification.paymentId()).isEqualTo("pay-store");
    }

    @Test
    @DisplayName("Should return all created notifications")
    void shouldReturnAllNotifications() {
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
        notificationService.processPaymentSucceededEvent(event1);
        notificationService.processPaymentSucceededEvent(event2);
        notificationService.processPaymentSucceededEvent(event3);

        List<Notification> notifications = notificationService.getAllNotifications();

        // Then
        assertThat(notifications).hasSize(3);
        assertThat(notifications).extracting(Notification::orderId)
                .containsExactly("order-1", "order-2", "order-3");
        assertThat(notifications).extracting(Notification::paymentId)
                .containsExactly("pay-1", "pay-2", "pay-3");
    }

    @Test
    @DisplayName("Should return empty list when no notifications exist")
    void shouldReturnEmptyListWhenNoNotifications() {
        // When
        List<Notification> notifications = notificationService.getAllNotifications();

        // Then
        assertNotNull(notifications);
        assertTrue(notifications.isEmpty());
    }

    @Test
    @DisplayName("Should handle multiple notifications for same order")
    void shouldHandleMultipleNotificationsForSameOrder() {
        // Given
        PaymentSucceededEvent event1 = new PaymentSucceededEvent(
                "order-same", "pay-1", 100.0, LocalDateTime.now()
        );
        PaymentSucceededEvent event2 = new PaymentSucceededEvent(
                "order-same", "pay-2", 100.0, LocalDateTime.now()
        );

        // When
        notificationService.processPaymentSucceededEvent(event1);
        notificationService.processPaymentSucceededEvent(event2);

        // Then
        List<Notification> notifications = notificationService.getAllNotifications();
        assertThat(notifications).hasSize(2);
        assertThat(notifications).allMatch(n -> n.orderId().equals("order-same"));
        assertNotEquals(notifications.get(0).notificationId(), notifications.get(1).notificationId());
    }

    @Test
    @DisplayName("Should process notification with zero amount")
    void shouldProcessNotificationWithZeroAmount() {
        // Given
        PaymentSucceededEvent event = new PaymentSucceededEvent(
                "order-zero", "pay-zero", 0.0, LocalDateTime.now()
        );

        // When
        notificationService.processPaymentSucceededEvent(event);

        // Then
        List<Notification> notifications = notificationService.getAllNotifications();
        assertThat(notifications).hasSize(1);
        assertThat(notifications.get(0).message()).contains("0.00");
    }

    @Test
    @DisplayName("Should process notification with large amount")
    void shouldProcessNotificationWithLargeAmount() {
        // Given
        PaymentSucceededEvent event = new PaymentSucceededEvent(
                "order-large", "pay-large", 999999.99, LocalDateTime.now()
        );

        // When
        notificationService.processPaymentSucceededEvent(event);

        // Then
        List<Notification> notifications = notificationService.getAllNotifications();
        assertThat(notifications).hasSize(1);
        assertThat(notifications.get(0).message()).contains("999999.99");
    }

    @Test
    @DisplayName("Should set notification timestamp close to current time")
    void shouldSetNotificationTimestampCloseToCurrentTime() {
        // Given
        PaymentSucceededEvent event = new PaymentSucceededEvent(
                "order-time", "pay-time", 100.0, LocalDateTime.now()
        );
        LocalDateTime before = LocalDateTime.now();

        // When
        notificationService.processPaymentSucceededEvent(event);

        // Then
        LocalDateTime after = LocalDateTime.now();
        List<Notification> notifications = notificationService.getAllNotifications();
        Notification notification = notifications.get(0);

        assertThat(notification.timestamp()).isAfterOrEqualTo(before);
        assertThat(notification.timestamp()).isBeforeOrEqualTo(after);
    }

    @Test
    @DisplayName("Should maintain order of processed notifications")
    void shouldMaintainOrderOfProcessedNotifications() {
        // Given
        PaymentSucceededEvent event1 = new PaymentSucceededEvent(
                "order-first", "pay-first", 100.0, LocalDateTime.now()
        );
        PaymentSucceededEvent event2 = new PaymentSucceededEvent(
                "order-second", "pay-second", 200.0, LocalDateTime.now()
        );
        PaymentSucceededEvent event3 = new PaymentSucceededEvent(
                "order-third", "pay-third", 300.0, LocalDateTime.now()
        );

        // When
        notificationService.processPaymentSucceededEvent(event1);
        notificationService.processPaymentSucceededEvent(event2);
        notificationService.processPaymentSucceededEvent(event3);

        List<Notification> notifications = notificationService.getAllNotifications();

        // Then
        assertThat(notifications).hasSize(3);
        assertThat(notifications.get(0).orderId()).isEqualTo("order-first");
        assertThat(notifications.get(1).orderId()).isEqualTo("order-second");
        assertThat(notifications.get(2).orderId()).isEqualTo("order-third");
    }

    @Test
    @DisplayName("Should format amount with two decimal places in message")
    void shouldFormatAmountWithTwoDecimalPlaces() {
        // Given
        PaymentSucceededEvent event = new PaymentSucceededEvent(
                "order-format", "pay-format", 99.9, LocalDateTime.now()
        );

        // When
        notificationService.processPaymentSucceededEvent(event);

        // Then
        List<Notification> notifications = notificationService.getAllNotifications();
        assertThat(notifications.get(0).message()).contains("99.90");
    }

    @Test
    @DisplayName("Should verify notification ID is UUID format")
    void shouldVerifyNotificationIdIsUuidFormat() {
        // Given
        PaymentSucceededEvent event = new PaymentSucceededEvent(
                "order-uuid", "pay-uuid", 100.0, LocalDateTime.now()
        );

        // When
        notificationService.processPaymentSucceededEvent(event);

        // Then
        List<Notification> notifications = notificationService.getAllNotifications();
        String notificationId = notifications.get(0).notificationId();

        // UUID format: xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx
        assertThat(notificationId).matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$");
    }

    @Test
    @DisplayName("Should include all event details in notification")
    void shouldIncludeAllEventDetailsInNotification() {
        // Given
        String orderId = "order-complete";
        String paymentId = "pay-complete";
        Double amount = 175.25;
        PaymentSucceededEvent event = new PaymentSucceededEvent(
                orderId, paymentId, amount, LocalDateTime.now()
        );

        // When
        notificationService.processPaymentSucceededEvent(event);

        // Then
        List<Notification> notifications = notificationService.getAllNotifications();
        Notification notification = notifications.get(0);

        assertThat(notification.orderId()).isEqualTo(orderId);
        assertThat(notification.paymentId()).isEqualTo(paymentId);
        assertThat(notification.message()).contains(orderId);
        assertThat(notification.message()).contains(paymentId);
        assertThat(notification.message()).contains("175.25");
    }

    @Test
    @DisplayName("Should create notification with descriptive message")
    void shouldCreateNotificationWithDescriptiveMessage() {
        // Given
        PaymentSucceededEvent event = new PaymentSucceededEvent(
                "order-desc", "pay-desc", 50.0, LocalDateTime.now()
        );

        // When
        notificationService.processPaymentSucceededEvent(event);

        // Then
        List<Notification> notifications = notificationService.getAllNotifications();
        String message = notifications.get(0).message();

        assertThat(message).isNotEmpty();
        assertThat(message.toLowerCase()).contains("payment");
        assertThat(message.toLowerCase()).contains("successful");
        assertThat(message.toLowerCase()).contains("order");
    }
}

