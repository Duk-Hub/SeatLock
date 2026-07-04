package com.seatlock.global.exception;

import com.seatlock.global.response.ApiResponse;
import com.seatlock.global.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException e, HttpServletRequest request) {
        log.info("BusinessException: {} {} {}", e.getErrorCode().name(), request.getMethod(), request.getRequestURI());
        return toResponse(e.getErrorCode(), request, null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException e, HttpServletRequest request) {
        List<ErrorResponse.FieldError> fieldErrors = e.getBindingResult().getFieldErrors().stream()
                .map(error -> new ErrorResponse.FieldError(error.getField(), error.getDefaultMessage()))
                .toList();
        log.info("Validation failed: {} {} {}", request.getMethod(), request.getRequestURI(), fieldErrors);
        return toResponse(ErrorCode.INVALID_INPUT, request, fieldErrors);
    }

    @ExceptionHandler(PessimisticLockingFailureException.class)
    public ResponseEntity<ApiResponse<Void>> handlePessimisticLockingFailureException(PessimisticLockingFailureException e, HttpServletRequest request) {
        log.warn("PessimisticLockingFailureException: {} {} {}", request.getMethod(), request.getRequestURI(), e.getMessage());
        return toResponse(ErrorCode.LOCK_TIMEOUT, request, null);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoResourceFoundException(NoResourceFoundException e, HttpServletRequest request) {
        return toResponse(ErrorCode.NOT_FOUND, request, null);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodNotSupportedException(HttpRequestMethodNotSupportedException e, HttpServletRequest request) {
        return toResponse(ErrorCode.METHOD_NOT_ALLOWED, request, null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e, HttpServletRequest request) {
        log.error("Unexpected exception: {} {}", request.getMethod(), request.getRequestURI(), e);
        return toResponse(ErrorCode.INTERNAL_SERVER_ERROR, request, null);
    }

    private ResponseEntity<ApiResponse<Void>> toResponse(ErrorCode errorCode, HttpServletRequest request, List<ErrorResponse.FieldError> fieldErrors) {
        ErrorResponse errorResponse = ErrorResponse.of(errorCode.name(), errorCode.getMessage(), request.getRequestURI(), MDC.get("traceId"), fieldErrors);
        return ResponseEntity.status(errorCode.getStatus())
                .body(ApiResponse.fail(errorResponse));
    }
}
