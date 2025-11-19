package sg.com.gic.orderprocessingsystem.payment.controller;

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
import sg.com.gic.orderprocessingsystem.payment.dto.PaymentResponse;
import sg.com.gic.orderprocessingsystem.payment.service.PaymentService;

@RestController
@RequestMapping("/api/payments")
@Tag(name = "Payments", description = "Payment processing and retrieval endpoints")
public class PaymentController {

  private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);
  private final PaymentService paymentService;

  public PaymentController(PaymentService paymentService) {
    this.paymentService = paymentService;
  }

  @GetMapping
  @Operation(summary = "Get all payments", description = "Retrieves a list of all processed payments")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully retrieved payments",
          content = @Content(schema = @Schema(implementation = PaymentResponse.class)))
  })
  public ResponseEntity<List<PaymentResponse>> getAllPayments() {
    logger.info("Received request to get all payments");

    List<PaymentResponse> responses = paymentService.getAllPayments().stream()
        .map(payment -> new PaymentResponse(
            payment.paymentId(),
            payment.orderId(),
            payment.amount(),
            payment.timestamp()
        ))
        .collect(Collectors.toList());

    return ResponseEntity.ok(responses);
  }
}
