package sg.com.gic.orderprocessingsystem.order.controller;

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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sg.com.gic.orderprocessingsystem.exception.ErrorResponse;
import sg.com.gic.orderprocessingsystem.order.dto.CreateOrderRequest;
import sg.com.gic.orderprocessingsystem.order.dto.OrderResponse;
import sg.com.gic.orderprocessingsystem.order.domain.Order;
import sg.com.gic.orderprocessingsystem.order.service.OrderService;

@RestController
@RequestMapping("/api/orders")
@Tag(name = "Orders", description = "Order management endpoints")
public class OrderController {

  private static final Logger logger = LoggerFactory.getLogger(OrderController.class);
  private final OrderService orderService;

  public OrderController(OrderService orderService) {
    this.orderService = orderService;
  }

  @PostMapping("/{orderId}/resend")
  @Operation(summary = "Resend an order create event",description = "Re-publishes the create order event for an existing order")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Resend triggered",
          content = @Content(schema = @Schema(implementation = OrderResponse.class))),
      @ApiResponse(responseCode = "404", description = "Order not found",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  public ResponseEntity<OrderResponse> resendOrder(@PathVariable String orderId){
     logger.info("Received request to resend order create event for orderId={}", orderId);

     Order order = orderService.resendOrder(orderId);

     OrderResponse response = new OrderResponse(
         order.orderId(),
         order.amount(),
         order.customerEmail(),
         order.createdAt()
     );

     logger.info("Resend triggered for orderId={}", orderId);
     return ResponseEntity.ok(response);
  }

  @PostMapping
  @Operation(summary = "Create a new order", description = "Creates a new order and publishes an CreateOrderRequest")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Order created successfully",
          content = @Content(schema = @Schema(implementation = OrderResponse.class))),
      @ApiResponse(responseCode = "400", description = "Invalid request data",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  public ResponseEntity<OrderResponse> createOrder(@RequestBody CreateOrderRequest request) {
    logger.info("Received request to create order: amount={}, email={}",
        request.amount(), request.customerEmail());

    Order order = orderService.createOrder(request.amount(), request.customerEmail());

    OrderResponse response = new OrderResponse(
        order.orderId(),
        order.amount(),
        order.customerEmail(),
        order.createdAt()
    );

    logger.info("Order created: {}", order.orderId());
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @GetMapping
  @Operation(summary = "Get all orders", description = "Retrieves a list of all orders")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully retrieved orders",
          content = @Content(schema = @Schema(implementation = OrderResponse.class)))
  })
  public ResponseEntity<List<OrderResponse>> getAllOrders() {
    logger.info("Received request to get all orders");

    List<OrderResponse> responses = orderService.getAllOrders().stream()
        .map(order -> new OrderResponse(
            order.orderId(),
            order.amount(),
            order.customerEmail(),
            order.createdAt()
        ))
        .collect(Collectors.toList());

    return ResponseEntity.ok(responses);
  }
}
