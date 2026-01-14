package sg.com.gic.orderprocessingsystem.notification.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name="notifications")
public class NotificationEntity {

  @Id
  @Column(name = "notification_id", nullable = false, unique = true)
  private String notificationId;

  @Column(name = "orderId", nullable = false)
  private String orderId;

  @Column(name = "paymentId", nullable = false)
  private String paymentId;

  @Column(name = "message", nullable = false)
  private String message;

  @Column(name= "timestamp", nullable = false)
  private LocalDateTime timestamp;

  protected NotificationEntity(){

  }

  public NotificationEntity(String notificationId, String orderId, String paymentId, String message, LocalDateTime timestamp) {
    this.notificationId = notificationId;
    this.orderId = orderId;
    this.paymentId = paymentId;
    this.message = message;
    this.timestamp = timestamp;
  }

  public String getNotificationId() {
    return notificationId;
  }

  public void setNotificationId(String notificationId) {
    this.notificationId = notificationId;
  }

  public String getOrderId() {
    return orderId;
  }

  public void setOrderId(String orderId) {
    this.orderId = orderId;
  }

  public String getPaymentId() {
    return paymentId;
  }

  public void setPaymentId(String paymentId) {
    this.paymentId = paymentId;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public LocalDateTime getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(LocalDateTime timestamp) {
    this.timestamp = timestamp;
  }
}
