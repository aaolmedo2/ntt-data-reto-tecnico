package com.data.ntt.account_service.shared.exception;

public class CustomerNotFoundException extends RuntimeException {
	public CustomerNotFoundException(String message) {
		super(message);
	}
}
