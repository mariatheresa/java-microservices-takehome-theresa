package sg.com.gic.orderprocessingsystem.order.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import sg.com.gic.orderprocessingsystem.eventbus.EventPublisher;
import sg.com.gic.orderprocessingsystem.eventbus.event.OrderCreatedEvent;
import sg.com.gic.orderprocessingsystem.order.domain.Order;

@Service
public class OrderService {

  private final List<Order> orders = new ArrayList<>();
  private final EventPublisher eventPublisher;

  public OrderService(EventPublisher eventPublisher) {
    this.eventPublisher = eventPublisher;
  }

  public Order resendOrder(String orderId) {
    return orders.stream()
        .filter(o -> o.orderId().equals(orderId))
        .findFirst()
        .map(order -> {
          // Republish the event
          OrderCreatedEvent event = new OrderCreatedEvent(
              order.orderId(),
              order.amount(),
              order.customerEmail()
          );
          eventPublisher.publish(event);
          return order;
        })
        .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
  }

  public Order createOrder(Double amount, String customerEmail) {
    String orderId = UUID.randomUUID().toString();
    Order order = new Order(orderId, amount, customerEmail, LocalDateTime.now());
    orders.add(order);

    // Publish event synchronously so that subscriber exceptions propagate
    OrderCreatedEvent event = new OrderCreatedEvent(orderId, amount, customerEmail);
    eventPublisher.publish(event);

    return order;
  }

  public List<Order> getAllOrders() {
    return new ArrayList<>(orders);
  }
}
