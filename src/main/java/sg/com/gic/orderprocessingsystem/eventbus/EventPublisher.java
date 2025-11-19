package sg.com.gic.orderprocessingsystem.eventbus;

public interface EventPublisher {

  void publish(Object event);
}
