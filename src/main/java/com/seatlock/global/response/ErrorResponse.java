package com.seatlock.global.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        String code,
        String message,
        String path,
        String traceId,
        LocalDateTime timestamp,
        List<FieldError> fieldErrors
) {
    public static ErrorResponse of(String code, String message, String path, String traceId, List<FieldError> fieldErrors) {
        return new ErrorResponse(code, message, path, traceId, LocalDateTime.now(), fieldErrors);
    }

    public record FieldError(String field, String message) {}
}
