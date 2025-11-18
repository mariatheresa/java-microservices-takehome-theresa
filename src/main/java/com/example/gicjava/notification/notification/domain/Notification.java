package com.example.gicjava.notification.notification.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Notification(
    @JsonProperty("notificationId") String notificationId,
    @JsonProperty("orderId") String orderId,
    @JsonProperty("paymentId") String paymentId,
    @JsonProperty("message") String message,
    @JsonProperty("timestamp") LocalDateTime timestamp
) { }