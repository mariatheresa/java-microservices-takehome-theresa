package com.example.gicjava.e2e;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Iterator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.http.MediaType;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class OrderPaymentNotificationE2ETest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Test
  public void createOrder_then_payment_and_notification_should_be_created() throws Exception {
    // 1) Create an order
    String requestBody = "{\"amount\": 42.5, \"customerEmail\": \"e2e@example.com\"}";

    MvcResult createResult = mockMvc.perform(post("/api/orders")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody))
        .andExpect(status().isCreated())
        .andReturn();

    String createContent = createResult.getResponse().getContentAsString();
    JsonNode created = objectMapper.readTree(createContent);
    String orderId = created.path("orderId").asText();
    assertThat(orderId).isNotEmpty();

    // 2) Verify payment for that order exists
    MvcResult paymentsResult = mockMvc.perform(get("/api/payments"))
        .andExpect(status().isOk())
        .andReturn();

    String paymentsContent = paymentsResult.getResponse().getContentAsString();
    JsonNode payments = objectMapper.readTree(paymentsContent);

    boolean paymentFound = false;
    if (payments.isArray()) {
      Iterator<JsonNode> it = payments.elements();
      while (it.hasNext()) {
        JsonNode p = it.next();
        if (orderId.equals(p.path("orderId").asText())) {
          paymentFound = true;
          break;
        }
      }
    }

    assertThat(paymentFound).as("Payment for order %s should exist", orderId).isTrue();

    // 3) Verify notification for that order exists
    MvcResult notificationsResult = mockMvc.perform(get("/api/notifications"))
        .andExpect(status().isOk())
        .andReturn();

    String notificationsContent = notificationsResult.getResponse().getContentAsString();
    JsonNode notifications = objectMapper.readTree(notificationsContent);

    boolean notificationFound = false;
    if (notifications.isArray()) {
      Iterator<JsonNode> it = notifications.elements();
      while (it.hasNext()) {
        JsonNode n = it.next();
        if (orderId.equals(n.path("orderId").asText())) {
          notificationFound = true;
          break;
        }
      }
    }

    assertThat(notificationFound).as("Notification for order %s should exist", orderId).isTrue();
  }
}
