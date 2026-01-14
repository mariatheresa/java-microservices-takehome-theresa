package sg.com.gic.orderprocessingsystem.eventbus.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "outbox_events")
public class OutboxEventEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name="event_type", nullable = false)
  private String eventType;

  @Column(name="payload", nullable = false, columnDefinition = "TEXT")
  private String payload;

  @Column(name="created_at", nullable = false)
  private LocalDateTime createdAt;

  @Column(name="processed", nullable = false)
  private Boolean processed = false;

  protected OutboxEventEntity(){

  }

  public OutboxEventEntity(String eventType, String payload, LocalDateTime createdAt) {
    this.eventType = eventType;
    this.payload = payload;
    this.createdAt = createdAt;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getEventType() {
    return eventType;
  }

  public void setEventType(String eventType) {
    this.eventType = eventType;
  }

  public String getPayload() {
    return payload;
  }

  public void setPayload(String payload) {
    this.payload = payload;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public Boolean getProcessed() {
    return processed;
  }

  public void setProcessed(Boolean processed) {
    this.processed = processed;
  }
}
