package sg.com.gic.orderprocessingsystem.notification.listener;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import sg.com.gic.orderprocessingsystem.eventbus.EventSubscriber;
import sg.com.gic.orderprocessingsystem.eventbus.event.PaymentSucceededEvent;
import sg.com.gic.orderprocessingsystem.notification.service.NotificationService;

@Component
public class PaymentSucceededEventListener {

  private static final Logger logger = LoggerFactory.getLogger(PaymentSucceededEventListener.class);

  private final EventSubscriber eventSubscriber;
  private final NotificationService notificationService;

  public PaymentSucceededEventListener(EventSubscriber eventSubscriber,
      NotificationService notificationService) {
    this.eventSubscriber = eventSubscriber;
    this.notificationService = notificationService;
  }


  @PostConstruct
  public void init() {
    // Subscribe to PaymentSucceededEvent
    eventSubscriber.subscribe(PaymentSucceededEvent.class, this::handlePaymentSucceeded);
    logger.info("NotificationService subscribed to PaymentSucceededEvent");
  }

  public void handlePaymentSucceeded(PaymentSucceededEvent event) {
    logger.info("Received PaymentSucceededEvent for order: {}, payment: {}",
        event.orderId(), event.paymentId());

    notificationService.processPaymentSucceededEvent(event);
  }

}
