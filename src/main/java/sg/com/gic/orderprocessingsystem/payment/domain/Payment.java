package sg.com.gic.orderprocessingsystem.payment.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Payment(
    @JsonProperty("paymentId") String paymentId,
    @JsonProperty("orderId") String orderId,
    @JsonProperty("amount") Double amount,
    @JsonProperty("timestamp") LocalDateTime timestamp
) {

}