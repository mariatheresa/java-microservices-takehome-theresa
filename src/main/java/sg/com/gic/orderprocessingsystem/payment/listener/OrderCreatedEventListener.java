package sg.com.gic.orderprocessingsystem.payment.listener;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import sg.com.gic.orderprocessingsystem.eventbus.EventSubscriber;
import sg.com.gic.orderprocessingsystem.eventbus.event.OrderCreatedEvent;
import sg.com.gic.orderprocessingsystem.payment.service.PaymentService;

@Component
public class OrderCreatedEventListener {

  private static final Logger logger = LoggerFactory.getLogger(OrderCreatedEventListener.class);

  private final PaymentService paymentService;

  public OrderCreatedEventListener(PaymentService paymentService) {
    this.paymentService = paymentService;
  }

  @Async
  @EventListener
  public void handleOrderCreated(OrderCreatedEvent event) {
    logger.info("Received CreateOrderRequest for order: {}", event.orderId());
    paymentService.processOrderCreatedEvent(event);
  }

}
