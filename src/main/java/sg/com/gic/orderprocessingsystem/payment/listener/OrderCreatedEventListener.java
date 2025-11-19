package sg.com.gic.orderprocessingsystem.payment.listener;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import sg.com.gic.orderprocessingsystem.eventbus.EventSubscriber;
import sg.com.gic.orderprocessingsystem.eventbus.event.OrderCreatedEvent;
import sg.com.gic.orderprocessingsystem.payment.service.PaymentService;

@Component
public class OrderCreatedEventListener {

  private static final Logger logger = LoggerFactory.getLogger(OrderCreatedEventListener.class);


  private final EventSubscriber eventSubscriber;
  private final PaymentService paymentService;

  public OrderCreatedEventListener(EventSubscriber eventSubscriber, PaymentService paymentService) {
    this.eventSubscriber = eventSubscriber;
    this.paymentService = paymentService;
  }


  @PostConstruct
  public void init() {
    // Subscribe to CreateOrderRequest
    eventSubscriber.subscribe(OrderCreatedEvent.class, this::handleOrderCreated);
    logger.info("PaymentService subscribed to CreateOrderRequest");
  }

  public void handleOrderCreated(OrderCreatedEvent event) {
    logger.info("Received CreateOrderRequest for order: {}", event.orderId());
    paymentService.processOrderCreatedEvent(event);
  }

}
