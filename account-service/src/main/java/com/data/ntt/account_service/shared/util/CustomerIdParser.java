package com.data.ntt.account_service.shared.util;

import com.data.ntt.account_service.shared.exception.BadRequestException;

public final class CustomerIdParser {
	private CustomerIdParser() {
	}

	public static Long parseCustomerId(String identification) {
		if (identification == null || identification.isBlank()) {
			throw new BadRequestException("customerIdentification is required");
		}
		try {
			return Long.parseLong(identification);
		} catch (NumberFormatException ex) {
			throw new BadRequestException("customerIdentification must be numeric");
		}
	}
}
