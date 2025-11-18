package com.example.gicjava.order.order.adapter;

import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(name = "OrderResponse", description = "Response DTO for orders")
public record OrderResponse(
    @JsonProperty("orderId") @Schema(description = "Unique order identifier", example = "ord_123e4567") String orderId,
    @JsonProperty("amount") @Schema(description = "Order amount in USD", example = "49.99") Double amount,
    @JsonProperty("customerEmail") @Schema(description = "Customer email address", example = "alice@example.com") String customerEmail,
    @JsonProperty("createdAt") @Schema(description = "Order creation timestamp") LocalDateTime createdAt){}