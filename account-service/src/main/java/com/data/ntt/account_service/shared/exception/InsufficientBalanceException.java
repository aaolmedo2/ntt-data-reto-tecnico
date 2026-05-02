package com.data.ntt.account_service.shared.exception;

public class InsufficientBalanceException extends RuntimeException {
	public InsufficientBalanceException(String message) {
		super(message);
	}
}
