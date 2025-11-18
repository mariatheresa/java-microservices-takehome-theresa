package com.example.gicjava.order;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.gicjava.eventBus.common.EventSubscriber;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class OrderControllerSubscriberIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ApplicationContext ctx;

  @BeforeEach
  void registerThrowingSubscriber() {
    EventSubscriber subscriber = ctx.getBean(EventSubscriber.class);
    // Register a subscriber that always throws IllegalArgumentException when OrderCreatedEvent arrives
    subscriber.subscribe(com.example.gicjava.eventBus.common.event.OrderCreatedEvent.class, e -> {
      throw new IllegalArgumentException("subscriber sync error");
    });
  }

  @Test
  void whenSubscriberThrows_sync_illegalArgument_handledByGlobalHandler() throws Exception {
    String body = "{\"amount\": 5.0, \"customerEmail\": \"i@example.com\"}";

    mockMvc.perform(post("/api/orders")
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.message").value("subscriber sync error"));
  }
}
