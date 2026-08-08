package com.novaerp.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Standard enterprise Error Response representation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Standardized Error Response Envelope")
public class ErrorResponse {

    @Schema(description = "HTTP status code", example = "400")
    private int status;

    @Schema(description = "Machine-readable error code", example = "VALIDATION_FAILED")
    private String errorCode;

    @Schema(description = "Human-readable error description", example = "Input validation failed for one or more fields")
    private String message;

    @Schema(description = "Target API endpoint path where the error occurred", example = "/api/v1/invoices")
    private String path;

    @Schema(description = "Timestamp when the error occurred")
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    @Schema(description = "Field-level validation error details")
    private Map<String, String> fieldErrors;

    @Schema(description = "List of general constraint violations")
    private List<String> details;
}
