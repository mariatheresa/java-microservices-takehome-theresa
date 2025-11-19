package sg.com.gic.orderprocessingsystem.payment.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import sg.com.gic.orderprocessingsystem.eventbus.EventPublisher;
import sg.com.gic.orderprocessingsystem.eventbus.event.OrderCreatedEvent;
import sg.com.gic.orderprocessingsystem.eventbus.event.PaymentSucceededEvent;
import sg.com.gic.orderprocessingsystem.payment.domain.Payment;

@Service
public class PaymentService {

  private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);
  private final List<Payment> payments = new ArrayList<>();
  private final EventPublisher eventPublisher;

  public PaymentService(EventPublisher eventPublisher) {
    this.eventPublisher = eventPublisher;
  }

  public void processOrderCreatedEvent(OrderCreatedEvent event) {
    // Simulate payment processing delay
    processPayment();
    // Create payment record
    Payment payment = createPayment(event);
    // Save payment record
    save(payment);
    logger.info("Payment processed successfully: paymentId={}, orderId={}, amount={}",
        payment.paymentId(), event.orderId(), event.amount());
    // Publish PaymentSucceededEvent
    publishPaymentProcessedEvent(payment);
  }

  private void save(Payment payment) {
    payments.add(payment);
  }

  private Payment createPayment(OrderCreatedEvent event) {
    String paymentId = UUID.randomUUID().toString();
    LocalDateTime timestamp = LocalDateTime.now();

    return new Payment(paymentId, event.orderId(), event.amount(), timestamp);
  }

  private void processPayment() {
    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  private void publishPaymentProcessedEvent(Payment payment) {
    // Create PaymentSucceededEvent
    PaymentSucceededEvent paymentEvent = new PaymentSucceededEvent(
        payment.orderId(),
        payment.paymentId(),
        payment.amount(),
        payment.timestamp());

    // Publish PaymentSucceededEvent
    // Use synchronous publish so that any subscriber exceptions propagate
    eventPublisher.publish(paymentEvent);
    logger.info("PaymentSucceededEvent published for order: {}", payment.orderId());
  }

  public List<Payment> getAllPayments() {
    return payments;
  }
}
