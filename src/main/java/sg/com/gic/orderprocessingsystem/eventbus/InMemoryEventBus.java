package sg.com.gic.orderprocessingsystem.eventbus;

import jakarta.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class InMemoryEventBus implements EventPublisher, EventSubscriber {

  private static final Logger logger = LoggerFactory.getLogger(InMemoryEventBus.class);
  private final Map<Class<?>, List<Consumer<Object>>> subscribers = new ConcurrentHashMap<>();

  @Override
  public <T> void subscribe(Class<T> eventType, Consumer<T> handler) {
    subscribers.computeIfAbsent(eventType, k -> new ArrayList<>())
        .add((Consumer<Object>) handler);
    logger.info("Subscribed to event type: {}", eventType.getSimpleName());
  }

  @Async
  @Override
  public void publish(Object event) {
    Class<?> eventType = event.getClass();
    logger.info("Publishing event: {} - {}", eventType.getSimpleName(), event);

    List<Consumer<Object>> handlers = subscribers.get(eventType);
    if (handlers != null) {
      for (Consumer<Object> handler : handlers) {
        handler.accept(event);
      }
    }
  }

  @PreDestroy
  public void shutdown() {
    subscribers.clear();
    logger.debug("InMemoryEventBus subscribers cleared on shutdown");
  }
}
