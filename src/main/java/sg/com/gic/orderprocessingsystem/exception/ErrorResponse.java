package sg.com.gic.orderprocessingsystem.exception;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ErrorResponse(
    @JsonProperty("timestamp") LocalDateTime timestamp,
    @JsonProperty("status") int status,
    @JsonProperty("error") String error,
    @JsonProperty("message") String message,
    @JsonProperty("path") String path) {

}

