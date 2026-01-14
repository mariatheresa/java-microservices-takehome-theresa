package sg.com.gic.orderprocessingsystem.eventbus;

import java.util.function.Consumer;

public interface EventPublisher {

  void publish(Object event);

  <T> void subscribe(Class<T> eventType, Consumer<T> handler);
}
