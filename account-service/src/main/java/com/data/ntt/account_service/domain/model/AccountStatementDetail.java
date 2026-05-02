package com.data.ntt.account_service.domain.model;

import java.math.BigDecimal;
import java.util.List;

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
public class AccountStatementDetail {
	private String accountNumber;
	private AccountType accountType;
	private BigDecimal initialBalance;
	private BigDecimal availableBalance;
	private Boolean status;
	private List<Movement> movements;
}
