package com.data.ntt.account_service.interfaces.dto.mapper;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.data.ntt.account_service.domain.model.AccountStatement;
import com.data.ntt.account_service.domain.model.AccountStatementDetail;
import com.data.ntt.account_service.interfaces.dto.response.AccountStatementDetailResponse;
import com.data.ntt.account_service.interfaces.dto.response.AccountStatementResponse;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ReportMapper {
	private final MovementMapper movementMapper;

	public AccountStatementResponse toResponse(AccountStatement statement) {
		if (statement == null) {
			return null;
		}
		return AccountStatementResponse.builder()
				.customerIdentification(statement.getCustomerIdentification())
				.customerName(statement.getCustomerName())
				.startDate(statement.getStartDate())
				.endDate(statement.getEndDate())
				.accounts(mapDetails(statement.getAccounts()))
				.build();
	}

	private List<AccountStatementDetailResponse> mapDetails(List<AccountStatementDetail> details) {
		if (details == null) {
			return List.of();
		}
		return details.stream()
				.map(detail -> AccountStatementDetailResponse.builder()
						.accountNumber(detail.getAccountNumber())
						.accountType(detail.getAccountType())
						.initialBalance(detail.getInitialBalance())
						.availableBalance(detail.getAvailableBalance())
						.status(detail.getStatus())
						.movements(detail.getMovements() == null
								? List.of()
								: detail.getMovements().stream()
										.map(movementMapper::toResponse)
										.collect(Collectors.toList()))
						.build())
				.collect(Collectors.toList());
	}
}
