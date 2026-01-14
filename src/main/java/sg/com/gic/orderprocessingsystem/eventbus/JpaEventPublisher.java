package sg.com.gic.orderprocessingsystem.eventbus;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import sg.com.gic.orderprocessingsystem.eventbus.entity.OutboxEventEntity;
import sg.com.gic.orderprocessingsystem.eventbus.repository.OutboxEventRepository;

@Component
public class JpaEventPublisher  implements EventPublisher{
  private static final Logger logger = LoggerFactory.getLogger(JpaEventPublisher.class);

  private final OutboxEventRepository outboxEventRepository;
  private final ApplicationEventPublisher applicationEventPublisher;
  private final ObjectMapper objectMapper;

  public JpaEventPublisher(OutboxEventRepository outboxEventRepository,ApplicationEventPublisher applicationEventPublisher, ObjectMapper objectMapper) {
    this.outboxEventRepository = outboxEventRepository;
    this.applicationEventPublisher = applicationEventPublisher;
    this.objectMapper = objectMapper;
  }

  @Override
  public void publish(Object event) {
    if (event == null) throw new NullPointerException("event is null");

    try {
      String payload = objectMapper.writeValueAsString(event);
      OutboxEventEntity outboxEvent = new OutboxEventEntity(event.getClass().getSimpleName(), payload, LocalDateTime.now());
      if(event.getClass().getSimpleName().equals("PaymentSucceededEvent")){
        outboxEvent.setProcessed(true);
      }
      OutboxEventEntity saved = outboxEventRepository.save(outboxEvent);
      logger.info("Event persisted to outbox: type={}, id={}", saved.getEventType(), saved.getId());
      applicationEventPublisher.publishEvent(event);
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Failed to serialize event", e);
    }
  }

  @Override
  public <T> void subscribe(Class<T> eventType, Consumer<T> handler) {
    String typeName = eventType.getSimpleName();
    List<OutboxEventEntity> events = outboxEventRepository.findByEventTypeAndProcessedFalse(typeName);
    for (OutboxEventEntity eventEntity : events) {
      try {
        T event = objectMapper.readValue(eventEntity.getPayload(), eventType);
        handler.accept(event);
        eventEntity.setProcessed(true);
        outboxEventRepository.save(eventEntity);
      } catch (JsonProcessingException e) {
        throw new RuntimeException("Failed to deserialize outbox event payload", e);
      }
    }
    // No runtime in-memory registration — future publishes are not delivered in-process.
  }
}
