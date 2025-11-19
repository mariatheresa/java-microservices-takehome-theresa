package sg.com.gic.orderprocessingsystem.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.WebRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GlobalExceptionHandler Unit Tests")
class GlobalExceptionHandlerTest {

    @Mock
    private WebRequest webRequest;

    @InjectMocks
    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        when(webRequest.getDescription(false)).thenReturn("uri=/api/test");
    }

    @Test
    @DisplayName("Should handle IllegalArgumentException correctly")
    void shouldHandleIllegalArgumentException() {
        // Given
        IllegalArgumentException exception = new IllegalArgumentException("Invalid input");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleIllegalArgumentException(
                exception, webRequest
        );

        // Then
        assertNotNull(response);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        ErrorResponse body = response.getBody();
        assertNotNull(body);
        assertThat(body.status()).isEqualTo(400);
        assertThat(body.error()).isEqualTo("Bad Request");
        assertThat(body.message()).isEqualTo("Invalid input");
        assertThat(body.path()).isEqualTo("uri=/api/test");
        assertNotNull(body.timestamp());
    }

    @Test
    @DisplayName("Should handle NullPointerException correctly")
    void shouldHandleNullPointerException() {
        // Given
        NullPointerException exception = new NullPointerException("Null value encountered");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleNullPointerException(
                exception, webRequest
        );

        // Then
        assertNotNull(response);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);

        ErrorResponse body = response.getBody();
        assertNotNull(body);
        assertThat(body.status()).isEqualTo(500);
        assertThat(body.error()).isEqualTo("Internal Server Error");
        assertThat(body.message()).isEqualTo("A required value was null");
        assertThat(body.path()).isEqualTo("uri=/api/test");
        assertNotNull(body.timestamp());
    }

    @Test
    @DisplayName("Should handle generic Exception correctly")
    void shouldHandleGenericException() {
        // Given
        Exception exception = new Exception("Unexpected error");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleGlobalException(
                exception, webRequest
        );

        // Then
        assertNotNull(response);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);

        ErrorResponse body = response.getBody();
        assertNotNull(body);
        assertThat(body.status()).isEqualTo(500);
        assertThat(body.error()).isEqualTo("Internal Server Error");
        assertThat(body.message()).isEqualTo("An unexpected error occurred");
        assertThat(body.path()).isEqualTo("uri=/api/test");
        assertNotNull(body.timestamp());
    }

    @Test
    @DisplayName("Should handle IllegalArgumentException with empty message")
    void shouldHandleIllegalArgumentExceptionWithEmptyMessage() {
        // Given
        IllegalArgumentException exception = new IllegalArgumentException("");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleIllegalArgumentException(
                exception, webRequest
        );

        // Then
        assertNotNull(response);
        ErrorResponse body = response.getBody();
        assertNotNull(body);
        assertThat(body.message()).isEmpty();
    }

    @Test
    @DisplayName("Should handle IllegalArgumentException with null message")
    void shouldHandleIllegalArgumentExceptionWithNullMessage() {
        // Given
        IllegalArgumentException exception = new IllegalArgumentException((String) null);

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleIllegalArgumentException(
                exception, webRequest
        );

        // Then
        assertNotNull(response);
        ErrorResponse body = response.getBody();
        assertNotNull(body);
        assertNull(body.message());
    }

    @Test
    @DisplayName("Should handle RuntimeException as generic exception")
    void shouldHandleRuntimeException() {
        // Given
        RuntimeException exception = new RuntimeException("Runtime error");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleGlobalException(
                exception, webRequest
        );

        // Then
        assertNotNull(response);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);

        ErrorResponse body = response.getBody();
        assertNotNull(body);
        assertThat(body.status()).isEqualTo(500);
        assertThat(body.error()).isEqualTo("Internal Server Error");
    }

    @Test
    @DisplayName("Should set correct HTTP status for IllegalArgumentException")
    void shouldSetCorrectStatusForIllegalArgument() {
        // Given
        IllegalArgumentException exception = new IllegalArgumentException("Bad input");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleIllegalArgumentException(
                exception, webRequest
        );

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(400);
    }

    @Test
    @DisplayName("Should set correct HTTP status for NullPointerException")
    void shouldSetCorrectStatusForNullPointer() {
        // Given
        NullPointerException exception = new NullPointerException("Null error");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleNullPointerException(
                exception, webRequest
        );

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(500);
    }

    @Test
    @DisplayName("Should include request path in error response")
    void shouldIncludeRequestPathInErrorResponse() {
        // Given
        when(webRequest.getDescription(false)).thenReturn("uri=/api/orders/create");
        Exception exception = new Exception("Test error");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleGlobalException(
                exception, webRequest
        );

        // Then
        ErrorResponse body = response.getBody();
        assertNotNull(body);
        assertThat(body.path()).isEqualTo("uri=/api/orders/create");
    }

    @Test
    @DisplayName("Should handle IllegalArgumentException with long message")
    void shouldHandleIllegalArgumentExceptionWithLongMessage() {
        // Given
        String longMessage = "This is a very long error message that exceeds normal length. ".repeat(10);
        IllegalArgumentException exception = new IllegalArgumentException(longMessage);

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleIllegalArgumentException(
                exception, webRequest
        );

        // Then
        ErrorResponse body = response.getBody();
        assertNotNull(body);
        assertThat(body.message()).isEqualTo(longMessage);
    }

    @Test
    @DisplayName("Should handle IllegalArgumentException with special characters")
    void shouldHandleExceptionWithSpecialCharacters() {
        // Given
        IllegalArgumentException exception = new IllegalArgumentException(
                "Invalid input: <script>alert('xss')</script>"
        );

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleIllegalArgumentException(
                exception, webRequest
        );

        // Then
        ErrorResponse body = response.getBody();
        assertNotNull(body);
        assertThat(body.message()).contains("<script>");
    }

    @Test
    @DisplayName("Should create error response with all required fields")
    void shouldCreateErrorResponseWithAllFields() {
        // Given
        Exception exception = new Exception("Test");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleGlobalException(
                exception, webRequest
        );

        // Then
        ErrorResponse body = response.getBody();
        assertNotNull(body);
        assertNotNull(body.timestamp());
        assertNotNull(body.status());
        assertNotNull(body.error());
        assertNotNull(body.message());
        assertNotNull(body.path());
    }

    @Test
    @DisplayName("Should handle different IllegalArgumentException messages")
    void shouldHandleDifferentIllegalArgumentMessages() {
        // Given
        IllegalArgumentException ex1 = new IllegalArgumentException("Amount cannot be negative");
        IllegalArgumentException ex2 = new IllegalArgumentException("Email is invalid");
        IllegalArgumentException ex3 = new IllegalArgumentException("Order not found");

        // When
        ResponseEntity<ErrorResponse> response1 = exceptionHandler.handleIllegalArgumentException(ex1, webRequest);
        ResponseEntity<ErrorResponse> response2 = exceptionHandler.handleIllegalArgumentException(ex2, webRequest);
        ResponseEntity<ErrorResponse> response3 = exceptionHandler.handleIllegalArgumentException(ex3, webRequest);

        // Then
        assertThat(response1.getBody().message()).isEqualTo("Amount cannot be negative");
        assertThat(response2.getBody().message()).isEqualTo("Email is invalid");
        assertThat(response3.getBody().message()).isEqualTo("Order not found");
    }
}

