package com.example.gicjava.order.order.domain;

import com.example.gicjava.eventBus.common.EventPublisher;
import com.example.gicjava.eventBus.common.event.OrderCreatedEvent;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class OrderService {

  private final List<Order> orders = new ArrayList<>();
  private final EventPublisher eventPublisher;

  public OrderService(EventPublisher eventPublisher) {
    this.eventPublisher = eventPublisher;
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
