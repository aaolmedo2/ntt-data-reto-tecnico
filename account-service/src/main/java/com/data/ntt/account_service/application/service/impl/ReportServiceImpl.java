package com.data.ntt.account_service.application.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.data.ntt.account_service.application.service.ReportService;
import com.data.ntt.account_service.domain.model.AccountStatement;
import com.data.ntt.account_service.domain.model.AccountStatementDetail;
import com.data.ntt.account_service.domain.model.Movement;
import com.data.ntt.account_service.infrastructure.client.CustomerServiceClient;
import com.data.ntt.account_service.infrastructure.persistence.entity.AccountEntity;
import com.data.ntt.account_service.infrastructure.persistence.entity.MovementEntity;
import com.data.ntt.account_service.infrastructure.persistence.mapper.MovementEntityMapper;
import com.data.ntt.account_service.infrastructure.persistence.repository.AccountRepository;
import com.data.ntt.account_service.infrastructure.persistence.repository.MovementRepository;
import com.data.ntt.account_service.shared.exception.InvalidDateRangeException;
import com.data.ntt.account_service.shared.util.CustomerIdParser;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {
	private final CustomerServiceClient customerServiceClient;
	private final AccountRepository accountRepository;
	private final MovementRepository movementRepository;
	private final MovementEntityMapper movementEntityMapper;

	@Override
	public Mono<AccountStatement> getAccountStatement(String customerIdentification, LocalDate startDate,
			LocalDate endDate) {
		validateDateRange(startDate, endDate);
		return customerServiceClient.getCustomerByIdentification(customerIdentification)
				.flatMap(customer -> Mono.fromCallable(() -> {
					Long customerId = CustomerIdParser.parseCustomerId(customerIdentification);
					LocalDateTime start = LocalDateTime.of(startDate, LocalTime.MIN);
					LocalDateTime end = LocalDateTime.of(endDate, LocalTime.MAX);
					List<AccountEntity> accounts = accountRepository.findByCustomerIdAndStatusTrue(customerId);
					List<AccountStatementDetail> details = accounts.stream()
							.map(account -> buildAccountDetail(account, start, end))
							.collect(Collectors.toList());
					log.info("Account statement generated: customerId={}, accounts={}", customerId, details.size());
					return AccountStatement.builder()
							.customerIdentification(customerIdentification)
							.customerName(customer.getName())
							.startDate(startDate)
							.endDate(endDate)
							.accounts(details)
							.build();
				}).subscribeOn(Schedulers.boundedElastic()));
	}

	private AccountStatementDetail buildAccountDetail(AccountEntity account, LocalDateTime start, LocalDateTime end) {
		List<MovementEntity> movements = movementRepository
				.findByAccountAccountIdAndDateBetweenOrderByDateAsc(account.getAccountId(), start, end);
		List<Movement> movementDetails = movements.stream()
				.map(movementEntityMapper::toDomain)
				.collect(Collectors.toList());
		return AccountStatementDetail.builder()
				.accountNumber(account.getAccountNumber())
				.accountType(account.getAccountType())
				.initialBalance(account.getInitialBalance())
				.availableBalance(account.getAvailableBalance())
				.status(account.getStatus())
				.movements(movementDetails)
				.build();
	}

	private void validateDateRange(LocalDate startDate, LocalDate endDate) {
		if (startDate == null || endDate == null) {
			throw new InvalidDateRangeException("startDate and endDate are required");
		}
		if (startDate.isAfter(endDate)) {
			throw new InvalidDateRangeException("startDate must not be after endDate");
		}
		LocalDate today = LocalDate.now();
		if (endDate.isAfter(today)) {
			throw new InvalidDateRangeException("endDate no puede ser mayor a hoy");
		}
		long rangeDays = ChronoUnit.DAYS.between(startDate, endDate) + 1;
		if (rangeDays > 30) {
			throw new InvalidDateRangeException("El rango de fechas no puede ser mayor a 30 dias");
		}
	}
}
