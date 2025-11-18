package com.example.gicjava.eventBus.common;

import java.util.function.Consumer;

public interface EventSubscriber {

  <T> void subscribe(Class<T> eventType, Consumer<T> handler);
}
