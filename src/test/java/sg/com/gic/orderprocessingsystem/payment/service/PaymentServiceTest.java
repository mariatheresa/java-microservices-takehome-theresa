package sg.com.gic.orderprocessingsystem.payment.service;

import java.util.ArrayList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import sg.com.gic.orderprocessingsystem.eventbus.EventPublisher;
import sg.com.gic.orderprocessingsystem.eventbus.event.OrderCreatedEvent;
import sg.com.gic.orderprocessingsystem.eventbus.event.PaymentSucceededEvent;
import sg.com.gic.orderprocessingsystem.payment.domain.Payment;

import java.time.LocalDateTime;
import java.util.List;
import sg.com.gic.orderprocessingsystem.payment.entity.PaymentEntity;
import sg.com.gic.orderprocessingsystem.payment.repository.PaymentRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentService Unit Tests")
class PaymentServiceTest {

    @Mock
    private EventPublisher eventPublisher;

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private PaymentService paymentService;

    @Captor
    private ArgumentCaptor<PaymentSucceededEvent> eventCaptor;

    private List<PaymentEntity> inMemoryStore;

    @BeforeEach
    void setUp() {
        inMemoryStore = new ArrayList<>();

        // Mock the paymentRepository to store payments in an in-memory list

        lenient().when(paymentRepository.save(any(PaymentEntity.class))).thenAnswer(invocation -> {
           PaymentEntity entity = invocation.getArgument(0);
            inMemoryStore.add(entity);
            return entity;
        });

        lenient().when(paymentRepository.findAll()).thenReturn(inMemoryStore);

    }

    @Test
    @DisplayName("Should process OrderCreatedEvent successfully")
    void shouldProcessOrderCreatedEvent() {
        // Given
        OrderCreatedEvent event = new OrderCreatedEvent("order-123", 100.0, "test@example.com");

        // When
        paymentService.processOrderCreatedEvent(event);

        // Then
        List<Payment> payments = paymentService.getAllPayments();
        assertThat(payments).hasSize(1);
        assertThat(payments.get(0).orderId()).isEqualTo("order-123");
        assertThat(payments.get(0).amount()).isEqualTo(100.0);
        assertNotNull(payments.get(0).paymentId());
        assertNotNull(payments.get(0).timestamp());
    }

    @Test
    @DisplayName("Should publish PaymentSucceededEvent after processing payment")
    void shouldPublishPaymentSucceededEvent() {
        // Given
        OrderCreatedEvent event = new OrderCreatedEvent("order-456", 200.0, "publisher@example.com");

        // When
        paymentService.processOrderCreatedEvent(event);

        // Then
        verify(eventPublisher, times(1)).publish(eventCaptor.capture());
        PaymentSucceededEvent capturedEvent = eventCaptor.getValue();

        assertNotNull(capturedEvent);
        assertThat(capturedEvent.orderId()).isEqualTo("order-456");
        assertThat(capturedEvent.amount()).isEqualTo(200.0);
        assertNotNull(capturedEvent.paymentId());
        assertNotNull(capturedEvent.timestamp());
    }

    @Test
    @DisplayName("Should generate unique payment IDs for different orders")
    void shouldGenerateUniquePaymentIds() {
        // Given
        OrderCreatedEvent event1 = new OrderCreatedEvent("order-1", 100.0, "user1@example.com");
        OrderCreatedEvent event2 = new OrderCreatedEvent("order-2", 200.0, "user2@example.com");

        // When
        paymentService.processOrderCreatedEvent(event1);
        paymentService.processOrderCreatedEvent(event2);

        // Then
        List<Payment> payments = paymentService.getAllPayments();
        assertThat(payments).hasSize(2);
        assertNotEquals(payments.get(0).paymentId(), payments.get(1).paymentId());
    }

    @Test
    @DisplayName("Should store payment in internal list")
    void shouldStorePaymentInList() {
        // Given
        OrderCreatedEvent event = new OrderCreatedEvent("order-789", 150.0, "store@example.com");

        // When
        paymentService.processOrderCreatedEvent(event);
        List<Payment> payments = paymentService.getAllPayments();

        // Then
        assertThat(payments).hasSize(1);
        Payment payment = payments.get(0);
        assertThat(payment.orderId()).isEqualTo("order-789");
        assertThat(payment.amount()).isEqualTo(150.0);
    }

    @Test
    @DisplayName("Should return all processed payments")
    void shouldReturnAllPayments() {
        // Given
        OrderCreatedEvent event1 = new OrderCreatedEvent("order-1", 100.0, "user1@example.com");
        OrderCreatedEvent event2 = new OrderCreatedEvent("order-2", 200.0, "user2@example.com");
        OrderCreatedEvent event3 = new OrderCreatedEvent("order-3", 300.0, "user3@example.com");

        // When
        paymentService.processOrderCreatedEvent(event1);
        paymentService.processOrderCreatedEvent(event2);
        paymentService.processOrderCreatedEvent(event3);

        List<Payment> payments = paymentService.getAllPayments();

        // Then
        assertThat(payments).hasSize(3);
        assertThat(payments).extracting(Payment::orderId)
                .containsExactly("order-1", "order-2", "order-3");
        assertThat(payments).extracting(Payment::amount)
                .containsExactly(100.0, 200.0, 300.0);
    }

    @Test
    @DisplayName("Should return empty list when no payments processed")
    void shouldReturnEmptyListWhenNoPayments() {
        // When
        List<Payment> payments = paymentService.getAllPayments();

        // Then
        assertNotNull(payments);
        assertTrue(payments.isEmpty());
    }

    @Test
    @DisplayName("Should handle multiple payments for same order ID")
    void shouldHandleMultiplePaymentsForSameOrder() {
        // Given
        OrderCreatedEvent event1 = new OrderCreatedEvent("order-same", 100.0, "user@example.com");
        OrderCreatedEvent event2 = new OrderCreatedEvent("order-same", 100.0, "user@example.com");

        // When
        paymentService.processOrderCreatedEvent(event1);
        paymentService.processOrderCreatedEvent(event2);

        // Then
        List<Payment> payments = paymentService.getAllPayments();
        assertThat(payments).hasSize(2);
        assertThat(payments).allMatch(p -> p.orderId().equals("order-same"));
        assertNotEquals(payments.get(0).paymentId(), payments.get(1).paymentId());
    }

    @Test
    @DisplayName("Should process payment with zero amount")
    void shouldProcessPaymentWithZeroAmount() {
        // Given
        OrderCreatedEvent event = new OrderCreatedEvent("order-zero", 0.0, "zero@example.com");

        // When
        paymentService.processOrderCreatedEvent(event);

        // Then
        List<Payment> payments = paymentService.getAllPayments();
        assertThat(payments).hasSize(1);
        assertThat(payments.get(0).amount()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("Should process payment with large amount")
    void shouldProcessPaymentWithLargeAmount() {
        // Given
        OrderCreatedEvent event = new OrderCreatedEvent("order-large", 999999999.99, "large@example.com");

        // When
        paymentService.processOrderCreatedEvent(event);

        // Then
        List<Payment> payments = paymentService.getAllPayments();
        assertThat(payments).hasSize(1);
        assertThat(payments.get(0).amount()).isEqualTo(999999999.99);
    }

    @Test
    @DisplayName("Should set payment timestamp close to current time")
    void shouldSetPaymentTimestampCloseToCurrentTime() {
        // Given
        OrderCreatedEvent event = new OrderCreatedEvent("order-time", 100.0, "time@example.com");
        LocalDateTime before = LocalDateTime.now();

        // When
        paymentService.processOrderCreatedEvent(event);

        // Then
        LocalDateTime after = LocalDateTime.now().plusSeconds(2);
        List<Payment> payments = paymentService.getAllPayments();
        Payment payment = payments.get(0);

        assertThat(payment.timestamp()).isAfterOrEqualTo(before);
        assertThat(payment.timestamp()).isBeforeOrEqualTo(after);
    }

    @Test
    @DisplayName("Should publish event with payment details")
    void shouldPublishEventWithPaymentDetails() {
        // Given
        OrderCreatedEvent orderEvent = new OrderCreatedEvent("order-event", 250.0, "event@example.com");

        // When
        paymentService.processOrderCreatedEvent(orderEvent);

        // Then
        verify(eventPublisher, times(1)).publish(eventCaptor.capture());
        PaymentSucceededEvent publishedEvent = eventCaptor.getValue();

        assertThat(publishedEvent.orderId()).isEqualTo("order-event");
        assertThat(publishedEvent.amount()).isEqualTo(250.0);
        assertNotNull(publishedEvent.paymentId());
        assertNotNull(publishedEvent.timestamp());
    }

    @Test
    @DisplayName("Should maintain order of processed payments")
    void shouldMaintainOrderOfProcessedPayments() {
        // Given
        OrderCreatedEvent event1 = new OrderCreatedEvent("order-first", 100.0, "first@example.com");
        OrderCreatedEvent event2 = new OrderCreatedEvent("order-second", 200.0, "second@example.com");
        OrderCreatedEvent event3 = new OrderCreatedEvent("order-third", 300.0, "third@example.com");

        // When
        paymentService.processOrderCreatedEvent(event1);
        paymentService.processOrderCreatedEvent(event2);
        paymentService.processOrderCreatedEvent(event3);

        List<Payment> payments = paymentService.getAllPayments();

        // Then
        assertThat(payments).hasSize(3);
        assertThat(payments.get(0).orderId()).isEqualTo("order-first");
        assertThat(payments.get(1).orderId()).isEqualTo("order-second");
        assertThat(payments.get(2).orderId()).isEqualTo("order-third");
    }

    @Test
    @DisplayName("Should handle event publisher exception")
    void shouldPropagateEventPublisherException() {
        // Given
        OrderCreatedEvent event = new OrderCreatedEvent("order-exception", 100.0, "exception@example.com");
        doThrow(new RuntimeException("Event publishing failed"))
                .when(eventPublisher).publish(any(PaymentSucceededEvent.class));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            paymentService.processOrderCreatedEvent(event);
        });
    }

    @Test
    @DisplayName("Should process payment with decimal amount")
    void shouldProcessPaymentWithDecimalAmount() {
        // Given
        OrderCreatedEvent event = new OrderCreatedEvent("order-decimal", 99.99, "decimal@example.com");

        // When
        paymentService.processOrderCreatedEvent(event);

        // Then
        List<Payment> payments = paymentService.getAllPayments();
        assertThat(payments).hasSize(1);
        assertThat(payments.get(0).amount()).isEqualTo(99.99);
    }

    @Test
    @DisplayName("Should verify payment ID is UUID format")
    void shouldVerifyPaymentIdIsUuidFormat() {
        // Given
        OrderCreatedEvent event = new OrderCreatedEvent("order-uuid", 100.0, "uuid@example.com");

        // When
        paymentService.processOrderCreatedEvent(event);

        // Then
        List<Payment> payments = paymentService.getAllPayments();
        String paymentId = payments.get(0).paymentId();

        // UUID format: xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx
        assertThat(paymentId).matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$");
    }
}

