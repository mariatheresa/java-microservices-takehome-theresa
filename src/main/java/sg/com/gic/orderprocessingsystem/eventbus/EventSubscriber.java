package sg.com.gic.orderprocessingsystem.eventbus;

import java.util.function.Consumer;

public interface EventSubscriber {

  <T> void subscribe(Class<T> eventType, Consumer<T> handler);
}
