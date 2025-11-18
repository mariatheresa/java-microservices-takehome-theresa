package com.example.gicjava.order.order.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Order(
    @JsonProperty("orderId") String orderId,
    @JsonProperty("amount") Double amount,
    @JsonProperty("customerEmail") String customerEmail,
    @JsonProperty("createdAt") LocalDateTime createdAt
) {

}