package sg.com.gic.orderprocessingsystem.eventbus.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PaymentSucceededEvent(
    @JsonProperty("orderId") String orderId,
    @JsonProperty("paymentId") String paymentId,
    @JsonProperty("amount") Double amount,
    @JsonProperty("timestamp") java.time.LocalDateTime timestamp
) {

}
