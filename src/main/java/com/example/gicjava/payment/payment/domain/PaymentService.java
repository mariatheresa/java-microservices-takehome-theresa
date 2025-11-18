package com.example.gicjava.payment.payment.domain;

import com.example.gicjava.eventBus.common.EventPublisher;
import com.example.gicjava.eventBus.common.EventSubscriber;
import com.example.gicjava.eventBus.common.event.OrderCreatedEvent;
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
public class PaymentService {

  private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);
  private final List<Payment> payments = new ArrayList<>();
  private final EventPublisher eventPublisher;
  private final EventSubscriber eventSubscriber;

  public PaymentService(EventPublisher eventPublisher, EventSubscriber eventSubscriber) {
    this.eventPublisher = eventPublisher;
    this.eventSubscriber = eventSubscriber;
  }

  @PostConstruct
  public void init() {
    // Subscribe to OrderCreatedEvent
    eventSubscriber.subscribe(OrderCreatedEvent.class, this::handleOrderCreated);
    logger.info("PaymentService subscribed to OrderCreatedEvent");
  }

  public void handleOrderCreated(OrderCreatedEvent event) {
    logger.info("Received OrderCreatedEvent for order: {}", event.orderId());

    try {
      // Simulate payment processing delay
      Thread.sleep(1000);

      // Process payment
      String paymentId = UUID.randomUUID().toString();
      LocalDateTime timestamp = LocalDateTime.now();

      Payment payment = new Payment(paymentId, event.orderId(), event.amount(), timestamp);
      payments.add(payment);

      logger.info("Payment processed successfully: paymentId={}, orderId={}, amount={}",
          paymentId, event.orderId(), event.amount());

      // Publish PaymentSucceededEvent
      PaymentSucceededEvent paymentEvent = new PaymentSucceededEvent(
          event.orderId(),
          paymentId,
          event.amount(),
          timestamp);

      // Use synchronous publish so that any subscriber exceptions propagate
      eventPublisher.publish(paymentEvent);
      logger.info("PaymentSucceededEvent published for order: {}", event.orderId());

    } catch (InterruptedException e) {
      logger.error("Payment processing interrupted for order: {}", event.orderId(), e);
      Thread.currentThread().interrupt();
    } catch (Exception e) {
      logger.error("Failed to process payment for order: {}", event.orderId(), e);
      // Preserve runtime exception types so GlobalExceptionHandler can match them (e.g., IllegalArgumentException -> 400)
      if (e instanceof RuntimeException) {
        throw (RuntimeException) e;
      }
      throw new RuntimeException(e);
    }
  }

  public List<Payment> getAllPayments() {
    return new ArrayList<>(payments);
  }
}
