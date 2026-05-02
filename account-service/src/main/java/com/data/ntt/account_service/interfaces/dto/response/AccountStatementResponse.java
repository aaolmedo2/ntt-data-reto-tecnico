package com.data.ntt.account_service.interfaces.dto.response;

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
public class AccountStatementResponse {
	private String customerIdentification;
	private String customerName;
	private LocalDate startDate;
	private LocalDate endDate;
	private List<AccountStatementDetailResponse> accounts;
}
