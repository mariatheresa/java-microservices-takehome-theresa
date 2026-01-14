package sg.com.gic.orderprocessingsystem.eventbus;

import java.util.function.Consumer;

@Deprecated
public interface EventSubscriber {

  <T> void subscribe(Class<T> eventType, Consumer<T> handler);
}
