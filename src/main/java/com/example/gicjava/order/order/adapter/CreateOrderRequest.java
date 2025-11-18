package com.example.gicjava.order.order.adapter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CreateOrderRequest(
    @JsonProperty("amount") Double amount,
    @JsonProperty("customerEmail") String customerEmail) {}
