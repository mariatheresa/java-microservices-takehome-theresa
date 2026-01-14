package sg.com.gic.orderprocessingsystem.order.service;

import java.util.ArrayList;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import sg.com.gic.orderprocessingsystem.eventbus.EventPublisher;
import sg.com.gic.orderprocessingsystem.eventbus.event.OrderCreatedEvent;
import sg.com.gic.orderprocessingsystem.order.domain.Order;

import java.time.LocalDateTime;
import java.util.List;
import sg.com.gic.orderprocessingsystem.order.entity.OrderEntity;
import sg.com.gic.orderprocessingsystem.order.repository.OrderRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService Unit Tests")
class OrderServiceTest {

    @Mock
    private EventPublisher eventPublisher;

    @InjectMocks
    private OrderService orderService;

    @Mock
    private OrderRepository orderRepository;

    @Captor
    private ArgumentCaptor<OrderCreatedEvent> eventCaptor;

    private List<OrderEntity> savedOrders;

    @BeforeEach
    void setUp() {
        savedOrders = new ArrayList<>();

        // Mock save behavior
        lenient().when(orderRepository.save(any(OrderEntity.class)))
            .thenAnswer(invocation -> {
                OrderEntity entity = invocation.getArgument(0);
                savedOrders.add(entity);
                return entity;
            });

        // Mock findAll behavior
        lenient().when(orderRepository.findAll())
            .thenAnswer(invocation -> new ArrayList<>(savedOrders));
    }

    @Test
    @DisplayName("Should republish OrderCreatedEvent when resending order")
    void shouldRepublishEventWhenResendingOrder() {
        // Given
        Order created = orderService.createOrder(120.0, "gas@gmail.com");

        when(orderRepository.findById(created.orderId()))
            .thenReturn(Optional.of(savedOrders.get(0)));

        Mockito.clearInvocations(eventPublisher);

        // When
        Order resent = orderService.resendOrder(created.orderId());

        // Then
        verify(eventPublisher, times(1)).publish(eventCaptor.capture());
        OrderCreatedEvent captured = eventCaptor.getValue();
        assertEquals(created.orderId(), captured.orderId());
    }

    @Test
    @DisplayName("Should create order with valid amount and email")
    void shouldCreateOrderSuccessfully() {
        // Given
        Double amount = 100.0;
        String email = "test@example.com";

        // When
        Order order = orderService.createOrder(amount, email);

        // Then
        assertNotNull(order);
        assertNotNull(order.orderId());
        assertEquals(amount, order.amount());
        assertEquals(email, order.customerEmail());
        assertNotNull(order.createdAt());
        assertTrue(order.createdAt().isBefore(LocalDateTime.now().plusSeconds(1)));
    }

    @Test
    @DisplayName("Should generate unique order IDs for multiple orders")
    void shouldGenerateUniqueOrderIds() {
        // When
        Order order1 = orderService.createOrder(100.0, "user1@example.com");
        Order order2 = orderService.createOrder(200.0, "user2@example.com");
        Order order3 = orderService.createOrder(300.0, "user3@example.com");

        // Then
        assertNotEquals(order1.orderId(), order2.orderId());
        assertNotEquals(order1.orderId(), order3.orderId());
        assertNotEquals(order2.orderId(), order3.orderId());
    }

    @Test
    @DisplayName("Should publish OrderCreatedEvent when order is created")
    void shouldPublishOrderCreatedEvent() {
        // Given
        Double amount = 150.0;
        String email = "publisher@example.com";

        // When
        Order order = orderService.createOrder(amount, email);

        // Then
        verify(eventPublisher, times(1)).publish(eventCaptor.capture());
        OrderCreatedEvent capturedEvent = eventCaptor.getValue();

        assertNotNull(capturedEvent);
        assertEquals(order.orderId(), capturedEvent.orderId());
        assertEquals(amount, capturedEvent.amount());
        assertEquals(email, capturedEvent.customerEmail());
    }

    @Test
    @DisplayName("Should store order in internal list")
    void shouldStoreOrderInList() {
        // Given
        Double amount = 75.0;
        String email = "stored@example.com";

        // When
        Order createdOrder = orderService.createOrder(amount, email);
        List<Order> allOrders = orderService.getAllOrders();

        // Then
        assertThat(allOrders).hasSize(1);
        assertThat(allOrders).extracting(Order::orderId).contains(createdOrder.orderId());
        assertThat(allOrders).extracting(Order::amount).contains(amount);
        assertThat(allOrders).extracting(Order::customerEmail).contains(email);
    }

    @Test
    @DisplayName("Should return all created orders")
    void shouldReturnAllOrders() {
        // Given
        orderService.createOrder(100.0, "user1@example.com");
        orderService.createOrder(200.0, "user2@example.com");
        orderService.createOrder(300.0, "user3@example.com");

        // When
        List<Order> orders = orderService.getAllOrders();

        // Then
        assertThat(orders).hasSize(3);
        assertThat(orders).extracting(Order::amount).containsExactly(100.0, 200.0, 300.0);
        assertThat(orders).extracting(Order::customerEmail)
                .containsExactly("user1@example.com", "user2@example.com", "user3@example.com");
    }

    @Test
    @DisplayName("Should return empty list when no orders exist")
    void shouldReturnEmptyListWhenNoOrders() {
        // When
        List<Order> orders = orderService.getAllOrders();

        // Then
        assertNotNull(orders);
        assertTrue(orders.isEmpty());
    }

    @Test
    @DisplayName("Should return defensive copy of orders list")
    void shouldReturnDefensiveCopyOfOrdersList() {
        // Given
        orderService.createOrder(100.0, "test@example.com");

        // When
        List<Order> orders1 = orderService.getAllOrders();
        List<Order> orders2 = orderService.getAllOrders();

        // Then
        assertNotSame(orders1, orders2, "Should return different list instances");
        assertEquals(orders1, orders2, "Should contain same orders");
    }

    @Test
    @DisplayName("Should create order with zero amount")
    void shouldCreateOrderWithZeroAmount() {
        // Given
        Double amount = 0.0;
        String email = "zero@example.com";

        // When
        Order order = orderService.createOrder(amount, email);

        // Then
        assertNotNull(order);
        assertEquals(0.0, order.amount());
        assertEquals(email, order.customerEmail());
    }

    @Test
    @DisplayName("Should create order with negative amount")
    void shouldCreateOrderWithNegativeAmount() {
        // Given
        Double amount = -50.0;
        String email = "negative@example.com";

        // When
        Order order = orderService.createOrder(amount, email);

        // Then
        assertNotNull(order);
        assertEquals(-50.0, order.amount());
    }

    @Test
    @DisplayName("Should create order with very large amount")
    void shouldCreateOrderWithLargeAmount() {
        // Given
        Double amount = 999999999.99;
        String email = "large@example.com";

        // When
        Order order = orderService.createOrder(amount, email);

        // Then
        assertNotNull(order);
        assertEquals(999999999.99, order.amount());
    }

    @Test
    @DisplayName("Should create order with special characters in email")
    void shouldCreateOrderWithSpecialCharactersInEmail() {
        // Given
        String specialEmail = "test+special.name@sub-domain.example.com";

        // When
        Order order = orderService.createOrder(100.0, specialEmail);

        // Then
        assertNotNull(order);
        assertEquals(specialEmail, order.customerEmail());
    }

    @Test
    @DisplayName("Should maintain order of created orders")
    void shouldMaintainOrderOfCreatedOrders() {
        // Given & When
        Order order1 = orderService.createOrder(100.0, "first@example.com");
        Order order2 = orderService.createOrder(200.0, "second@example.com");
        Order order3 = orderService.createOrder(300.0, "third@example.com");

        List<Order> orders = orderService.getAllOrders();

        // Then
        assertThat(orders).hasSize(3);
        assertThat(orders.get(0).orderId()).isEqualTo(order1.orderId());
        assertThat(orders.get(1).orderId()).isEqualTo(order2.orderId());
        assertThat(orders.get(2).orderId()).isEqualTo(order3.orderId());
    }

    @Test
    @DisplayName("Should set creation timestamp close to current time")
    void shouldSetCreationTimestampCloseToCurrentTime() {
        // Given
        LocalDateTime before = LocalDateTime.now();

        // When
        Order order = orderService.createOrder(100.0, "time@example.com");

        // Then
        LocalDateTime after = LocalDateTime.now();
        assertThat(order.createdAt()).isAfterOrEqualTo(before);
        assertThat(order.createdAt()).isBeforeOrEqualTo(after);
    }

    @Test
    @DisplayName("Should handle event publisher exception by propagating it")
    void shouldPropagateEventPublisherException() {
        // Given
        doThrow(new RuntimeException("Event publishing failed"))
                .when(eventPublisher).publish(any(OrderCreatedEvent.class));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            orderService.createOrder(100.0, "exception@example.com");
        });
    }

    @Test
    @DisplayName("Should create multiple orders with same email")
    void shouldCreateMultipleOrdersWithSameEmail() {
        // Given
        String email = "repeat@example.com";

        // When
        Order order1 = orderService.createOrder(100.0, email);
        Order order2 = orderService.createOrder(200.0, email);

        // Then
        assertNotEquals(order1.orderId(), order2.orderId());
        assertEquals(email, order1.customerEmail());
        assertEquals(email, order2.customerEmail());

        List<Order> orders = orderService.getAllOrders();
        assertThat(orders).hasSize(2);
        assertThat(orders).extracting(Order::customerEmail).containsOnly(email);
    }

    @Test
    @DisplayName("Should publish event with correct event data")
    void shouldPublishEventWithCorrectData() {
        // Given
        Double amount = 250.75;
        String email = "event@example.com";

        // When
        orderService.createOrder(amount, email);

        // Then
        verify(eventPublisher, times(1)).publish(eventCaptor.capture());
        OrderCreatedEvent event = eventCaptor.getValue();

        assertNotNull(event.orderId());
        assertEquals(amount, event.amount());
        assertEquals(email, event.customerEmail());
    }
}

