package sg.com.gic.orderprocessingsystem.exception;

import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
      IllegalArgumentException ex, WebRequest request) {
    logger.error("Invalid argument: {}", ex.getMessage());

    ErrorResponse error = new ErrorResponse(
        LocalDateTime.now(),
        HttpStatus.BAD_REQUEST.value(),
        "Bad Request",
        ex.getMessage(),
        request.getDescription(false)
    );

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
  }

  @ExceptionHandler(NullPointerException.class)
  public ResponseEntity<ErrorResponse> handleNullPointerException(
      NullPointerException ex, WebRequest request) {
    logger.error("Null pointer exception: {}", ex.getMessage(), ex);

    ErrorResponse error = new ErrorResponse(
        LocalDateTime.now(),
        HttpStatus.INTERNAL_SERVER_ERROR.value(),
        "Internal Server Error",
        "A required value was null",
        request.getDescription(false)
    );

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
  }

  @ExceptionHandler(NoResourceFoundException.class)
  public ResponseEntity<ErrorResponse> handleNoResourceFoundException(
      NoResourceFoundException ex, WebRequest request) {
    logger.warn("Resource not found: {}", ex.getMessage());

    ErrorResponse error = new ErrorResponse(
        LocalDateTime.now(),
        HttpStatus.NOT_FOUND.value(),
        "Not Found",
        "The requested resource was not found",
        request.getDescription(false)
    );

    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGlobalException(
      Exception ex, WebRequest request) {
    logger.error("Unexpected error: {}", ex.getMessage(), ex);

    ErrorResponse error = new ErrorResponse(
        LocalDateTime.now(),
        HttpStatus.INTERNAL_SERVER_ERROR.value(),
        "Internal Server Error",
        "An unexpected error occurred",
        request.getDescription(false)
    );

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
  }
}

