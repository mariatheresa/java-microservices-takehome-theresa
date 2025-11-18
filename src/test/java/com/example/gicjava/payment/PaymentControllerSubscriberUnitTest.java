package com.example.gicjava.payment;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.gicjava.eventBus.common.EventPublisher;
import com.example.gicjava.order.order.adapter.OrderController;
import com.example.gicjava.order.order.domain.OrderService;
import com.example.gicjava.common.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

public class PaymentControllerSubscriberUnitTest {

  private MockMvc mockMvc;
  private EventPublisher eventPublisher;

  @BeforeEach
  void setup() {
    eventPublisher = Mockito.mock(EventPublisher.class);
    // use OrderController -> creating an order triggers the full synchronous flow (order -> payment -> notification)
    OrderService orderService = new OrderService(eventPublisher);
    OrderController controller = new OrderController(orderService);
    this.mockMvc = MockMvcBuilders.standaloneSetup(controller)
        .setControllerAdvice(new GlobalExceptionHandler())
        .build();
  }

  @Test
  void whenSubscriberThrows_illegalArgument_handledByGlobalHandler_400() throws Exception {
    doThrow(new IllegalArgumentException("subscriber error")).when(eventPublisher).publish(any());

    String body = "{\"amount\": 12.34, \"customerEmail\": \"x@example.com\"}";

    mockMvc.perform(post("/api/orders")
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
         .andExpect(status().isBadRequest())
         .andExpect(jsonPath("$.status").value(400))
         .andExpect(jsonPath("$.message").value("subscriber error"));
   }

   @Test
   void whenSubscriberThrows_nullPointer_handledByGlobalHandler_500() throws Exception {
     doThrow(new NullPointerException("npe")).when(eventPublisher).publish(any());
    String body = "{\"amount\": 12.34, \"customerEmail\": \"x@example.com\"}";

    mockMvc.perform(post("/api/orders")
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
         .andExpect(status().isInternalServerError())
         .andExpect(jsonPath("$.status").value(500))
         .andExpect(jsonPath("$.message").value("A required value was null"));
   }
 }
