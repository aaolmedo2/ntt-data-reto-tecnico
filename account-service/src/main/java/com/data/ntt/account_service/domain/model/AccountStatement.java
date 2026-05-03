package com.data.ntt.account_service.domain.model;

import java.time.LocalDate;
import java.util.List;

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
public class AccountStatement {
	private String customerIdentification;
	private String customerName;
	private LocalDate startDate;
	private LocalDate endDate;
	private List<AccountStatementDetail> accounts;
}
