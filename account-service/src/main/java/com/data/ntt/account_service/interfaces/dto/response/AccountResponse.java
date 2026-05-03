package com.data.ntt.account_service.interfaces.dto.response;

import java.math.BigDecimal;

import com.data.ntt.account_service.domain.enums.AccountType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountResponse {
	private String accountNumber;
	private AccountType accountType;
	private BigDecimal initialBalance;
	private BigDecimal availableBalance;
	private Boolean status;
	private CustomerSummaryResponse customer;
}
