package com.example.gicjava.payment.payment.adapter;

import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(name = "PaymentResponse", description = "Response DTO for payments")
public record PaymentResponse(
    @JsonProperty("paymentId") @Schema(description = "Unique payment identifier", example = "pay_987e6543") String paymentId,
    @JsonProperty("orderId") @Schema(description = "Associated order ID", example = "ord_123e4567") String orderId,
    @JsonProperty("amount") @Schema(description = "Payment amount in USD", example = "49.99") Double amount,
    @JsonProperty("timestamp") @Schema(description = "Payment timestamp") LocalDateTime timestamp) {}