package com.data.ntt.account_service.shared.exception;

public class BadRequestException extends RuntimeException {
	public BadRequestException(String message) {
		super(message);
	}
}
