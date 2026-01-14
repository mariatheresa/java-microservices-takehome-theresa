package sg.com.gic.orderprocessingsystem.order.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table (name = "orders")
public class OrderEntity {

  @Id
  @Column(name = "order_id", nullable = false)
  private String orderId;

  @Column(name = "amount", nullable = false)
  private Double amount;

  @Column(name = "customer_email", nullable = false)
  private String customerEmail;

  @Column(name = "created_at", nullable = false)
  private String createdAt;

  protected OrderEntity() {
  }

  public OrderEntity(String orderId, Double amount, String customerEmail, String createdAt) {
    this.orderId = orderId;
    this.amount = amount;
    this.customerEmail = customerEmail;
    this.createdAt = createdAt;
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

  public String getCustomerEmail() {
    return customerEmail;
  }

  public void setCustomerEmail(String customerEmail) {
    this.customerEmail = customerEmail;
  }

  public String getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(String createdAt) {
    this.createdAt = createdAt;
  }
}
