package com.example.gicjava.order.domain;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import com.example.gicjava.eventBus.common.EventPublisher;
import com.example.gicjava.eventBus.common.event.OrderCreatedEvent;
import com.example.gicjava.order.order.domain.Order;
import com.example.gicjava.order.order.domain.OrderService;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

  @Mock
  private EventPublisher eventPublisher;

  @InjectMocks
  private OrderService orderService;

  @Test
  void createOrder_shouldStoreOrderAndPublishEvent() {
    // Arrange
    Double amount = 45.5;
    String email = "u@example.com";

    // Act
    Order order = orderService.createOrder(amount, email);

    // Assert
    Assertions.assertNotNull(order.orderId());
    Assertions.assertEquals(amount, order.amount());
    Assertions.assertEquals(email, order.customerEmail());
    Assertions.assertNotNull(order.createdAt());

    List<Order> orders = orderService.getAllOrders();
    Assertions.assertFalse(orders.isEmpty());

    // Verify event published using the synchronous API (publishSync) introduced earlier
    verify(eventPublisher).publish(any(OrderCreatedEvent.class));
  }
}
