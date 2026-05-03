package com.data.ntt.account_service.shared.exception;

public class AccountNotFoundException extends RuntimeException {
	public AccountNotFoundException(String message) {
		super(message);
	}
}
