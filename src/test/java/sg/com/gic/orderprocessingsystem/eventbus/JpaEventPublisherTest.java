package sg.com.gic.orderprocessingsystem.eventbus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import sg.com.gic.orderprocessingsystem.eventbus.entity.OutboxEventEntity;
import sg.com.gic.orderprocessingsystem.eventbus.event.OrderCreatedEvent;
import sg.com.gic.orderprocessingsystem.eventbus.event.PaymentSucceededEvent;
import sg.com.gic.orderprocessingsystem.eventbus.repository.OutboxEventRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("JpaEventPublisher Unit Tests")
class JpaEventPublisherTest {

  private JpaEventPublisher eventBus;

  @Mock
  private OutboxEventRepository outboxRepo;

  @Mock
  private ApplicationEventPublisher applicationEventPublisher;



  private ObjectMapper mapper;

  @BeforeEach
  void setUp() {
    mapper = new ObjectMapper();
    mapper.registerModule(new JavaTimeModule());
    eventBus = new JpaEventPublisher(outboxRepo, applicationEventPublisher,mapper);

    // by default, let save just return the entity back (mimic JPA save behavior)
    when(outboxRepo.save(any(OutboxEventEntity.class))).thenAnswer(inv -> {
      OutboxEventEntity e = inv.getArgument(0);
      e.setId(1L);
      return e;
    });
  }

  @Test
  @DisplayName("publish() should persist event to outbox with correct type and payload")
  void publishPersistsEvent() {
    // Given
    OrderCreatedEvent ev = new OrderCreatedEvent("order-123", 42.5, "a@b.com");

    // When
    eventBus.publish(ev);

    // Then
    ArgumentCaptor<OutboxEventEntity> captor = ArgumentCaptor.forClass(OutboxEventEntity.class);
    verify(outboxRepo, times(1)).save(captor.capture());
    OutboxEventEntity saved = captor.getValue();
    assertThat(saved.getEventType()).isEqualTo(OrderCreatedEvent.class.getSimpleName());
    assertThat(saved.getPayload()).contains("order-123");
    assertThat(saved.getPayload()).contains("a@b.com");
    assertThat(saved.getCreatedAt()).isNotNull();
    assertThat(saved.getProcessed()).isFalse();
  }

  @Test
  @DisplayName("subscribe() should process existing outbox events and mark them processed")
  void subscribeProcessesExistingOutboxEvents() throws Exception {
    // Given: create a serialized PaymentSucceededEvent payload in the outbox
    PaymentSucceededEvent payment = new PaymentSucceededEvent("order-1", "pay-1", 10.0, LocalDateTime.now());
    String payload = mapper.writeValueAsString(payment);

    OutboxEventEntity entity = new OutboxEventEntity(PaymentSucceededEvent.class.getSimpleName(), payload, LocalDateTime.now());
    entity.setProcessed(false);

    when(outboxRepo.findByEventTypeAndProcessedFalse(PaymentSucceededEvent.class.getSimpleName()))
        .thenReturn(List.of(entity));

    List<PaymentSucceededEvent> received = new ArrayList<>();
    CountDownLatch latch = new CountDownLatch(1);
    Consumer<PaymentSucceededEvent> handler = e -> {
      received.add(e);
      latch.countDown();
    };

    // When
    eventBus.subscribe(PaymentSucceededEvent.class, handler);

    // Then: handler must have been called synchronously during subscribe and the entity saved as processed
    assertThat(latch.await(1, TimeUnit.SECONDS)).isTrue();
    assertThat(received).hasSize(1);

    ArgumentCaptor<OutboxEventEntity> captor = ArgumentCaptor.forClass(OutboxEventEntity.class);
    verify(outboxRepo, atLeastOnce()).save(captor.capture());

    // one of the saved entities should be marked processed=true
    boolean anyProcessed = captor.getAllValues().stream().anyMatch(OutboxEventEntity::getProcessed);
    assertThat(anyProcessed).isTrue();
  }
}