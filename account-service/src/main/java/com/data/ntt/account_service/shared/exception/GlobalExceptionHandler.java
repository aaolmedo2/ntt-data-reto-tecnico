package com.data.ntt.account_service.shared.exception;

import java.time.Instant;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ServerWebInputException;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.http.server.reactive.ServerHttpRequest;

import com.data.ntt.account_service.interfaces.dto.response.ErrorResponse;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
	@ExceptionHandler(AccountNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleAccountNotFound(AccountNotFoundException ex, ServerHttpRequest request) {
		return buildError(HttpStatus.NOT_FOUND, ex.getMessage(), request, ex);
	}

	@ExceptionHandler(CustomerNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleCustomerNotFound(CustomerNotFoundException ex,
			ServerHttpRequest request) {
		return buildError(HttpStatus.NOT_FOUND, ex.getMessage(), request, ex);
	}

	@ExceptionHandler(AccountAlreadyExistsException.class)
	public ResponseEntity<ErrorResponse> handleAccountAlreadyExists(AccountAlreadyExistsException ex,
			ServerHttpRequest request) {
		return buildError(HttpStatus.CONFLICT, ex.getMessage(), request, ex);
	}

	@ExceptionHandler({ InvalidMovementException.class, InvalidDateRangeException.class, BadRequestException.class })
	public ResponseEntity<ErrorResponse> handleBadRequest(RuntimeException ex, ServerHttpRequest request) {
		return buildError(HttpStatus.BAD_REQUEST, ex.getMessage(), request, ex);
	}

	@ExceptionHandler(InsufficientBalanceException.class)
	public ResponseEntity<ErrorResponse> handleInsufficientBalance(InsufficientBalanceException ex,
			ServerHttpRequest request) {
		return buildError(HttpStatus.BAD_REQUEST, ex.getMessage(), request, ex);
	}

	@ExceptionHandler(CustomerServiceException.class)
	public ResponseEntity<ErrorResponse> handleCustomerService(CustomerServiceException ex, ServerHttpRequest request) {
		return buildError(HttpStatus.BAD_GATEWAY, ex.getMessage(), request, ex);
	}

	@ExceptionHandler(WebExchangeBindException.class)
	public ResponseEntity<ErrorResponse> handleValidation(WebExchangeBindException ex, ServerHttpRequest request) {
		String message = ex.getFieldErrors().stream()
				.map(error -> error.getField() + ": " + error.getDefaultMessage())
				.collect(Collectors.joining(", "));
		return buildError(HttpStatus.BAD_REQUEST, message, request, ex);
	}

	@ExceptionHandler(ServerWebInputException.class)
	public ResponseEntity<ErrorResponse> handleWebInput(ServerWebInputException ex, ServerHttpRequest request) {
		String reason = ex.getReason() != null ? ex.getReason() : "Invalid request";
		return buildError(HttpStatus.BAD_REQUEST, reason, request, ex);
	}

	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex,
			ServerHttpRequest request) {
		return buildError(HttpStatus.BAD_REQUEST, ex.getMessage(), request, ex);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex, ServerHttpRequest request) {
		return buildError(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred", request, ex);
	}

	private ResponseEntity<ErrorResponse> buildError(HttpStatus status, String message, ServerHttpRequest request,
			Exception ex) {
		if (status.is5xxServerError()) {
			log.error("Unhandled error", ex);
		} else {
			log.warn("Request error: {}", message);
		}
		ErrorResponse response = ErrorResponse.builder()
				.timestamp(Instant.now())
				.status(status.value())
				.error(status.getReasonPhrase())
				.message(message)
				.path(request.getPath().value())
				.build();
		return ResponseEntity.status(status).body(response);
	}
}
