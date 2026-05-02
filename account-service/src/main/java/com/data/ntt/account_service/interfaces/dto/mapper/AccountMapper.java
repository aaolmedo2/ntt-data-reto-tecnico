package com.data.ntt.account_service.interfaces.dto.mapper;

import org.springframework.stereotype.Component;

import com.data.ntt.account_service.domain.model.Account;
import com.data.ntt.account_service.interfaces.dto.request.AccountPatchRequest;
import com.data.ntt.account_service.interfaces.dto.request.AccountRequest;
import com.data.ntt.account_service.interfaces.dto.response.AccountResponse;
import com.data.ntt.account_service.interfaces.dto.response.CustomerSummaryResponse;

@Component
public class AccountMapper {
	public Account toDomain(AccountRequest request) {
		if (request == null) {
			return null;
		}
		return Account.builder()
				.accountNumber(request.getAccountNumber())
				.accountType(request.getAccountType())
				.initialBalance(request.getInitialBalance())
				.availableBalance(request.getInitialBalance())
				.build();
	}

	public Account toPatchDomain(AccountPatchRequest request) {
		if (request == null) {
			return null;
		}
		return Account.builder()
				.accountType(request.getAccountType())
				.status(request.getStatus())
				.build();
	}

	public AccountResponse toResponse(Account account) {
		if (account == null) {
			return null;
		}
		String identification = account.getCustomerIdentification();
		if (identification == null && account.getCustomerId() != null) {
			identification = account.getCustomerId().toString();
		}
		CustomerSummaryResponse customer = null;
		if (account.getCustomerName() != null
				|| identification != null
				|| account.getCustomerStatus() != null
				|| account.getCustomerGender() != null
				|| account.getCustomerAddress() != null
				|| account.getCustomerPhone() != null) {
			customer = CustomerSummaryResponse.builder()
					.name(account.getCustomerName())
					.gender(account.getCustomerGender())
					.identification(identification)
					.address(account.getCustomerAddress())
					.phone(account.getCustomerPhone())
					.status(account.getCustomerStatus())
					.build();
		}
		return AccountResponse.builder()
				.accountNumber(account.getAccountNumber())
				.accountType(account.getAccountType())
				.initialBalance(account.getInitialBalance())
				.availableBalance(account.getAvailableBalance())
				.status(account.getStatus())
				.customer(customer)
				.build();
	}
}
