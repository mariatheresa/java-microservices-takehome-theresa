package com.example.gicjava.order;

import com.example.gicjava.GicJavaApplication;
import com.example.gicjava.eventBus.common.EventPublisher;
import com.example.gicjava.eventBus.common.EventSubscriber;
import com.example.gicjava.eventBus.common.event.OrderCreatedEvent;
import com.example.gicjava.order.order.domain.Order;
import com.example.gicjava.order.order.domain.OrderService;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest(classes = GicJavaApplication.class)
@EnableAsync
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class OrderServiceIntegrationTest {

  @Autowired
  private OrderService orderService;

  @Autowired
  private EventPublisher eventPublisher;

  @Autowired
  private EventSubscriber eventSubscriber;

  @Test
  void createOrder_shouldStoreOrderAndPublishOrderCreatedEvent() throws Exception {
    // Arrange
    CountDownLatch latch = new CountDownLatch(1);
    AtomicReference<OrderCreatedEvent> captured = new AtomicReference<>();

    eventSubscriber.subscribe(OrderCreatedEvent.class, (OrderCreatedEvent ev) -> {
      captured.set(ev);
      latch.countDown();
    });

    // Act
    Order order = orderService.createOrder(200.0, "int@example.com");

    // Wait for async publish
    boolean ok = latch.await(6, TimeUnit.SECONDS);
    Assertions.assertTrue(ok, "Timed out waiting for OrderCreatedEvent to be published");

    // Assert order stored
    List<Order> orders = orderService.getAllOrders();
    Assertions.assertFalse(orders.isEmpty());

    Order stored = orders.get(0);
    Assertions.assertEquals(order.orderId(), stored.orderId());
    Assertions.assertEquals(order.amount(), stored.amount());
    Assertions.assertEquals(order.customerEmail(), stored.customerEmail());

    // Assert event captured
    OrderCreatedEvent event = captured.get();
    Assertions.assertNotNull(event);
    Assertions.assertEquals(order.orderId(), event.orderId());
    Assertions.assertEquals(order.amount(), event.amount());
  }
}

