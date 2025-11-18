package com.example.gicjava.order.order.adapter;

import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ErrorResponse(
    @JsonProperty("timestamp") LocalDateTime timestamp,
    @JsonProperty("status") int status,
    @JsonProperty("error") String error,
    @JsonProperty("message") String message,
    @JsonProperty("path") String path) {}

