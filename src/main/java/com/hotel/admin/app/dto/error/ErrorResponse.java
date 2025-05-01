package com.hotel.admin.app.dto.error;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
@RequiredArgsConstructor // Creates constructor for final fields
@JsonInclude(JsonInclude.Include.NON_NULL) // Exclude null fields (like validationErrors) from JSON output
public class ErrorResponse {

    private final LocalDateTime timestamp = LocalDateTime.now(); // Set timestamp on creation
    private final int status;
    private final String error; // e.g., "Not Found", "Bad Request", "Conflict"
    private final String message;
    private final String path;

    // Use a Map to hold specific validation errors (field name -> error message)
    private Map<String, String> validationErrors;



    // Constructor including validation errors (used by the handler for MethodArgumentNotValidException)
     public ErrorResponse(int status, String error, String message, String path, Map<String, String> validationErrors) {
        this(status, error, message, path); // Call the other constructor
        this.validationErrors = validationErrors;
    }
}