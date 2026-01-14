package sg.com.gic.orderprocessingsystem.order.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import sg.com.gic.orderprocessingsystem.eventbus.EventPublisher;
import sg.com.gic.orderprocessingsystem.eventbus.event.OrderCreatedEvent;
import sg.com.gic.orderprocessingsystem.order.domain.Order;
import sg.com.gic.orderprocessingsystem.order.entity.OrderEntity;
import sg.com.gic.orderprocessingsystem.order.repository.OrderRepository;

@Service
public class OrderService {
  private final EventPublisher eventPublisher;
  private final OrderRepository orderRepository;

  public OrderService(EventPublisher eventPublisher, OrderRepository orderRepository) {
    this.orderRepository = orderRepository;
    this.eventPublisher = eventPublisher;
  }

  public Order resendOrder(String orderId) {
    return orderRepository.findById(orderId)
        .map(orderEntity -> {
          // Republish the event
          Order order = toDomain(orderEntity);
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

    orderRepository.save(toEntity(order));

    // Publish event synchronously so that subscriber exceptions propagate
    OrderCreatedEvent event = new OrderCreatedEvent(orderId, amount, customerEmail);
    eventPublisher.publish(event);

    return order;
  }

  public List<Order> getAllOrders() {
    return orderRepository.findAll()
        .stream()
        .map(this::toDomain)
        .collect(Collectors.toList());
  }

  private Order toDomain(OrderEntity orderEntity){
    return new Order(
        orderEntity.getOrderId(),
        orderEntity.getAmount(),
        orderEntity.getCustomerEmail(),
        LocalDateTime.parse(orderEntity.getCreatedAt())
    );
  }

  private OrderEntity toEntity(Order order){
    return new OrderEntity(
        order.orderId(),
        order.amount(),
        order.customerEmail(),
        order.createdAt().toString()
    );
  }
}
