package sg.com.gic.orderprocessingsystem.eventbus.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OrderCreatedEvent(
    @JsonProperty("orderId") String orderId,
    @JsonProperty("amount") Double amount,
    @JsonProperty("customerEmail") String customerEmail
) {

}
