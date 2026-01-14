package sg.com.gic.orderprocessingsystem.payment.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
public class PaymentEntity {

  @Id
  @Column(name = "payment_id", nullable = false, unique = true)
  private String paymentId;

  @Column(name = "order_id", nullable = false)
  private String orderId;

  @Column(name = "amount", nullable = false)
  private Double amount;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime created_at;

  protected PaymentEntity(){

  }

  public PaymentEntity(String paymentId, String orderId, Double amount, LocalDateTime created_at) {
    this.paymentId = paymentId;
    this.orderId = orderId;
    this.amount = amount;
    this.created_at = created_at;
  }

  public String getPaymentId() {
    return paymentId;
  }

  public void setPaymentId(String paymentId) {
    this.paymentId = paymentId;
  }

  public String getOrderId() {
    return orderId;
  }

  public void setOrderId(String orderId) {
    this.orderId = orderId;
  }

  public Double getAmount() {
    return amount;
  }

  public void setAmount(Double amount) {
    this.amount = amount;
  }

  public LocalDateTime getCreated_at() {
    return created_at;
  }

  public void setCreated_at(LocalDateTime created_at) {
    this.created_at = created_at;
  }
}
