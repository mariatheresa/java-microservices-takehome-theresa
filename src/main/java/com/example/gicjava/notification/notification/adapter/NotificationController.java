package com.example.gicjava.notification.notification.adapter;

import com.example.gicjava.notification.notification.domain.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
@Tag(name = "Notifications", description = "Notification retrieval endpoints")
public class NotificationController {

  private static final Logger logger = LoggerFactory.getLogger(NotificationController.class);
  private final NotificationService notificationService;

  public NotificationController(NotificationService notificationService) {
    this.notificationService = notificationService;
  }

  @GetMapping
  @Operation(summary = "Get all notifications", description = "Retrieves a list of all sent notifications")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully retrieved notifications",
          content = @Content(schema = @Schema(implementation = NotificationResponse.class)))
  })
  public ResponseEntity<List<NotificationResponse>> getAllNotifications() {
    logger.info("Received request to get all notifications");

    List<NotificationResponse> responses = notificationService.getAllNotifications().stream()
        .map(notification -> new NotificationResponse(
            notification.notificationId(),
            notification.orderId(),
            notification.paymentId(),
            notification.message(),
            notification.timestamp()
        ))
        .collect(Collectors.toList());

    return ResponseEntity.ok(responses);
  }
}
