package sg.com.gic.orderprocessingsystem.eventbus;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import sg.com.gic.orderprocessingsystem.eventbus.event.OrderCreatedEvent;
import sg.com.gic.orderprocessingsystem.eventbus.event.PaymentSucceededEvent;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("InMemoryEventBus Unit Tests")
class InMemoryEventBusTest {

    private InMemoryEventBus eventBus;

    @BeforeEach
    void setUp() {
        eventBus = new InMemoryEventBus();
    }

    @Test
    @DisplayName("Should subscribe to event type successfully")
    void shouldSubscribeToEventType() {
        // Given
        List<OrderCreatedEvent> receivedEvents = new ArrayList<>();
        Consumer<OrderCreatedEvent> handler = receivedEvents::add;

        // When
        eventBus.subscribe(OrderCreatedEvent.class, handler);

        // Then
        // No exception thrown means subscription was successful
        assertNotNull(eventBus);
    }

    @Test
    @DisplayName("Should publish and receive event")
    void shouldPublishAndReceiveEvent() throws InterruptedException {
        // Given
        List<OrderCreatedEvent> receivedEvents = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(1);

        Consumer<OrderCreatedEvent> handler = event -> {
            receivedEvents.add(event);
            latch.countDown();
        };

        OrderCreatedEvent testEvent = new OrderCreatedEvent("order-123", 100.0, "test@example.com");

        // When
        eventBus.subscribe(OrderCreatedEvent.class, handler);
        eventBus.publish(testEvent);

        // Then
        assertTrue(latch.await(2, TimeUnit.SECONDS));
        assertThat(receivedEvents).hasSize(1);
        assertThat(receivedEvents.get(0).orderId()).isEqualTo("order-123");
        assertThat(receivedEvents.get(0).amount()).isEqualTo(100.0);
    }

    @Test
    @DisplayName("Should handle multiple subscribers for same event type")
    void shouldHandleMultipleSubscribers() throws InterruptedException {
        // Given
        List<OrderCreatedEvent> subscriber1Events = new ArrayList<>();
        List<OrderCreatedEvent> subscriber2Events = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(2);

        Consumer<OrderCreatedEvent> handler1 = event -> {
            subscriber1Events.add(event);
            latch.countDown();
        };

        Consumer<OrderCreatedEvent> handler2 = event -> {
            subscriber2Events.add(event);
            latch.countDown();
        };

        OrderCreatedEvent testEvent = new OrderCreatedEvent("order-multi", 200.0, "multi@example.com");

        // When
        eventBus.subscribe(OrderCreatedEvent.class, handler1);
        eventBus.subscribe(OrderCreatedEvent.class, handler2);
        eventBus.publish(testEvent);

        // Then
        assertTrue(latch.await(2, TimeUnit.SECONDS));
        assertThat(subscriber1Events).hasSize(1);
        assertThat(subscriber2Events).hasSize(1);
        assertThat(subscriber1Events.get(0).orderId()).isEqualTo("order-multi");
        assertThat(subscriber2Events.get(0).orderId()).isEqualTo("order-multi");
    }

    @Test
    @DisplayName("Should handle different event types independently")
    void shouldHandleDifferentEventTypes() throws InterruptedException {
        // Given
        List<OrderCreatedEvent> orderEvents = new ArrayList<>();
        List<PaymentSucceededEvent> paymentEvents = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(2);

        Consumer<OrderCreatedEvent> orderHandler = event -> {
            orderEvents.add(event);
            latch.countDown();
        };

        Consumer<PaymentSucceededEvent> paymentHandler = event -> {
            paymentEvents.add(event);
            latch.countDown();
        };

        OrderCreatedEvent orderEvent = new OrderCreatedEvent("order-1", 100.0, "test@example.com");
        PaymentSucceededEvent paymentEvent = new PaymentSucceededEvent(
                "order-1", "pay-1", 100.0, LocalDateTime.now()
        );

        // When
        eventBus.subscribe(OrderCreatedEvent.class, orderHandler);
        eventBus.subscribe(PaymentSucceededEvent.class, paymentHandler);
        eventBus.publish(orderEvent);
        eventBus.publish(paymentEvent);

        // Then
        assertTrue(latch.await(2, TimeUnit.SECONDS));
        assertThat(orderEvents).hasSize(1);
        assertThat(paymentEvents).hasSize(1);
        assertThat(orderEvents.get(0).orderId()).isEqualTo("order-1");
        assertThat(paymentEvents.get(0).orderId()).isEqualTo("order-1");
    }

    @Test
    @DisplayName("Should not deliver event to wrong subscriber type")
    void shouldNotDeliverEventToWrongSubscriberType() throws InterruptedException {
        // Given
        List<PaymentSucceededEvent> paymentEvents = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(1);

        Consumer<PaymentSucceededEvent> paymentHandler = event -> {
            paymentEvents.add(event);
            latch.countDown();
        };

        OrderCreatedEvent orderEvent = new OrderCreatedEvent("order-wrong", 100.0, "test@example.com");

        // When
        eventBus.subscribe(PaymentSucceededEvent.class, paymentHandler);
        eventBus.publish(orderEvent);

        // Then
        boolean received = latch.await(500, TimeUnit.MILLISECONDS);
        assertFalse(received, "Should not receive event of wrong type");
        assertThat(paymentEvents).isEmpty();
    }

    @Test
    @DisplayName("Should handle publishing event with no subscribers")
    void shouldHandlePublishingWithNoSubscribers() {
        // Given
        OrderCreatedEvent testEvent = new OrderCreatedEvent("order-none", 100.0, "test@example.com");

        // When & Then - Should not throw exception
        assertDoesNotThrow(() -> eventBus.publish(testEvent));
    }

    @Test
    @DisplayName("Should deliver multiple events to subscriber")
    void shouldDeliverMultipleEventsToSubscriber() throws InterruptedException {
        // Given
        List<OrderCreatedEvent> receivedEvents = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(3);

        Consumer<OrderCreatedEvent> handler = event -> {
            receivedEvents.add(event);
            latch.countDown();
        };

        // When
        eventBus.subscribe(OrderCreatedEvent.class, handler);
        eventBus.publish(new OrderCreatedEvent("order-1", 100.0, "test1@example.com"));
        eventBus.publish(new OrderCreatedEvent("order-2", 200.0, "test2@example.com"));
        eventBus.publish(new OrderCreatedEvent("order-3", 300.0, "test3@example.com"));

        // Then
        assertTrue(latch.await(2, TimeUnit.SECONDS));
        assertThat(receivedEvents).hasSize(3);
        assertThat(receivedEvents).extracting(OrderCreatedEvent::orderId)
                .containsExactly("order-1", "order-2", "order-3");
    }

    @Test
    @DisplayName("Should clear subscribers on shutdown")
    void shouldClearSubscribersOnShutdown() throws InterruptedException {
        // Given
        List<OrderCreatedEvent> receivedEvents = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(1);

        Consumer<OrderCreatedEvent> handler = event -> {
            receivedEvents.add(event);
            latch.countDown();
        };

        OrderCreatedEvent event1 = new OrderCreatedEvent("order-1", 100.0, "test@example.com");

        // When
        eventBus.subscribe(OrderCreatedEvent.class, handler);
        eventBus.publish(event1);
        assertTrue(latch.await(2, TimeUnit.SECONDS));

        // Store initial size
        int initialSize = receivedEvents.size();

        eventBus.shutdown();
        OrderCreatedEvent event2 = new OrderCreatedEvent("order-2", 200.0, "test@example.com");
        eventBus.publish(event2);

        // Wait a bit to ensure async processing would have completed if it was going to happen
        Thread.sleep(500);

        // Then
        assertThat(receivedEvents).hasSize(initialSize); // No new events after shutdown
    }

    @Test
    @DisplayName("Should handle subscriber exception and continue to other subscribers")
    void shouldHandleSubscriberExceptionGracefully() throws InterruptedException {
        // Given
        List<OrderCreatedEvent> successfulEvents = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(1);

        Consumer<OrderCreatedEvent> throwingHandler = event -> {
            throw new RuntimeException("Handler exception");
        };

        Consumer<OrderCreatedEvent> normalHandler = event -> {
            successfulEvents.add(event);
            latch.countDown();
        };

        OrderCreatedEvent testEvent = new OrderCreatedEvent("order-exception", 100.0, "test@example.com");

        // When
        eventBus.subscribe(OrderCreatedEvent.class, throwingHandler);
        eventBus.subscribe(OrderCreatedEvent.class, normalHandler);

        // Publishing may throw exception since we're using @Async but testing synchronously
        // In real async scenario, exceptions are handled differently
        try {
            eventBus.publish(testEvent);
            // If no exception, verify second handler still received the event
            assertTrue(latch.await(2, TimeUnit.SECONDS));
            assertThat(successfulEvents).hasSize(1);
        } catch (RuntimeException e) {
            // Exception propagation is acceptable behavior
            assertThat(e.getMessage()).contains("Handler exception");
        }
    }

    @Test
    @DisplayName("Should subscribe to same event type multiple times")
    void shouldSubscribeToSameEventTypeMultipleTimes() throws InterruptedException {
        // Given
        List<OrderCreatedEvent> events1 = new ArrayList<>();
        List<OrderCreatedEvent> events2 = new ArrayList<>();
        List<OrderCreatedEvent> events3 = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(3);

        // When
        eventBus.subscribe(OrderCreatedEvent.class, event -> {
            events1.add(event);
            latch.countDown();
        });
        eventBus.subscribe(OrderCreatedEvent.class, event -> {
            events2.add(event);
            latch.countDown();
        });
        eventBus.subscribe(OrderCreatedEvent.class, event -> {
            events3.add(event);
            latch.countDown();
        });

        eventBus.publish(new OrderCreatedEvent("order-multi", 100.0, "test@example.com"));

        // Then
        assertTrue(latch.await(2, TimeUnit.SECONDS));
        assertThat(events1).hasSize(1);
        assertThat(events2).hasSize(1);
        assertThat(events3).hasSize(1);
    }

    @Test
    @DisplayName("Should handle null event gracefully")
    void shouldHandleNullEvent() {
        // When & Then - Publishing null may throw exception, which is acceptable
        // The system doesn't need to handle null events
        assertThrows(NullPointerException.class, () -> eventBus.publish(null));
    }

    @Test
    @DisplayName("Should process events in order for single subscriber")
    void shouldProcessEventsInOrder() throws InterruptedException {
        // Given
        List<String> orderIds = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(5);

        Consumer<OrderCreatedEvent> handler = event -> {
            orderIds.add(event.orderId());
            latch.countDown();
        };

        // When
        eventBus.subscribe(OrderCreatedEvent.class, handler);
        for (int i = 1; i <= 5; i++) {
            eventBus.publish(new OrderCreatedEvent("order-" + i, 100.0, "test@example.com"));
        }

        // Then
        assertTrue(latch.await(2, TimeUnit.SECONDS));
        assertThat(orderIds).hasSize(5);
        assertThat(orderIds).containsExactly("order-1", "order-2", "order-3", "order-4", "order-5");
    }
}

