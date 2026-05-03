package com.data.ntt.customer_service.shared.exception;

import com.data.ntt.customer_service.interfaces.dto.response.ErrorResponse;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.validation.ConstraintViolationException;
import java.time.Instant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.codec.DecodingException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebInputException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BadRequestException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleBadRequest(
            BadRequestException ex,
            ServerWebExchange exchange) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), exchange, ex);
    }

    @ExceptionHandler(ConflictException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleConflict(
            ConflictException ex,
            ServerWebExchange exchange) {
        return buildErrorResponse(HttpStatus.CONFLICT, ex.getMessage(), exchange, ex);
    }

    @ExceptionHandler(NotFoundException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleNotFound(
            NotFoundException ex,
            ServerWebExchange exchange) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), exchange, ex);
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleValidation(
            WebExchangeBindException ex,
            ServerWebExchange exchange) {
        String message = ex.getAllErrors().isEmpty()
                ? "Validation failed"
                : ex.getAllErrors().get(0).getDefaultMessage();
        return buildErrorResponse(HttpStatus.BAD_REQUEST, message, exchange, ex);
    }

    @ExceptionHandler({ DecodingException.class, ServerWebInputException.class })
    public Mono<ResponseEntity<ErrorResponse>> handleDecoding(
            Exception ex,
            ServerWebExchange exchange) {
        String message = resolveDecodingMessage(ex);
        return buildErrorResponse(HttpStatus.BAD_REQUEST, message, exchange, ex);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleConstraintViolation(
            ConstraintViolationException ex,
            ServerWebExchange exchange) {
        String message = ex.getConstraintViolations().isEmpty()
                ? "Validation failed"
                : ex.getConstraintViolations().iterator().next().getMessage();
        return buildErrorResponse(HttpStatus.BAD_REQUEST, message, exchange, ex);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleDataIntegrity(
            DataIntegrityViolationException ex,
            ServerWebExchange exchange) {
        return buildErrorResponse(HttpStatus.CONFLICT, "Data integrity violation", exchange, ex);
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ErrorResponse>> handleUnexpected(
            Exception ex,
            ServerWebExchange exchange) {
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred",
                exchange,
                ex);
    }

    private Mono<ResponseEntity<ErrorResponse>> buildErrorResponse(
            HttpStatus status,
            String message,
            ServerWebExchange exchange,
            Exception ex) {
        ErrorResponse response = ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .path(exchange.getRequest().getPath().value())
                .build();

        if (status.is5xxServerError()) {
            log.error("Request failed status={} path={} message={}",
                    status.value(), response.getPath(), message, ex);
        } else {
            log.warn("Request failed status={} path={} message={}",
                    status.value(), response.getPath(), message);
        }

        return Mono.just(ResponseEntity.status(status).body(response));
    }

    private String resolveDecodingMessage(Exception ex) {
        if (ex instanceof ServerWebInputException inputException && inputException.getReason() != null) {
            return inputException.getReason();
        }

        Throwable cause = ex.getCause();
        if (cause instanceof InvalidFormatException formatException
                && !formatException.getPath().isEmpty()) {
            String field = formatException.getPath().get(0).getFieldName();
            return String.format("Invalid value for field '%s'", field);
        }

        return "Invalid request payload";
    }
}
