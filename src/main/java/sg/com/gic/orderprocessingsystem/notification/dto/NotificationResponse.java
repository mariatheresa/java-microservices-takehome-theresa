package sg.com.gic.orderprocessingsystem.notification.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(name = "NotificationResponse", description = "Response DTO for notifications")
public record NotificationResponse(
    @JsonProperty("notificationId") @Schema(description = "Unique notification identifier", example = "notif_abc123") String notificationId,
    @JsonProperty("orderId") @Schema(description = "Associated order ID", example = "ord_123e4567") String orderId,
    @JsonProperty("paymentId") @Schema(description = "Associated payment ID", example = "pay_987e6543") String paymentId,
    @JsonProperty("message") @Schema(description = "Notification message", example = "Payment successful for order ord_123e4567") String message,
    @JsonProperty("timestamp") @Schema(description = "Notification timestamp") LocalDateTime timestamp
) {

}
