package com.data.ntt.account_service.application.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import com.data.ntt.account_service.application.service.MovementService;
import com.data.ntt.account_service.domain.enums.MovementType;
import com.data.ntt.account_service.domain.model.Movement;
import com.data.ntt.account_service.infrastructure.persistence.entity.AccountEntity;
import com.data.ntt.account_service.infrastructure.persistence.entity.MovementEntity;
import com.data.ntt.account_service.infrastructure.persistence.mapper.MovementEntityMapper;
import com.data.ntt.account_service.infrastructure.persistence.repository.AccountRepository;
import com.data.ntt.account_service.infrastructure.persistence.repository.MovementRepository;
import com.data.ntt.account_service.shared.exception.AccountNotFoundException;
import com.data.ntt.account_service.shared.exception.InsufficientBalanceException;
import com.data.ntt.account_service.shared.exception.InvalidDateRangeException;
import com.data.ntt.account_service.shared.exception.InvalidMovementException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Slf4j
@Service
@RequiredArgsConstructor
public class MovementServiceImpl implements MovementService {
	private final AccountRepository accountRepository;
	private final MovementRepository movementRepository;
	private final MovementEntityMapper movementEntityMapper;
	private final TransactionTemplate transactionTemplate;

	@Override
	public Mono<Movement> createMovement(String accountNumber, Movement movement) {
		return Mono.fromCallable(() -> transactionTemplate.execute(status -> {
			AccountEntity account = accountRepository.findByAccountNumberAndStatusTrue(accountNumber)
					.orElseThrow(() -> new AccountNotFoundException(
							"Account with number '" + accountNumber + "' not found"));
			validateMovement(movement);
			BigDecimal newBalance = calculateNewBalance(account.getAvailableBalance(), movement.getType(),
					movement.getAmount());
			account.setAvailableBalance(newBalance);
			MovementEntity entity = MovementEntity.builder()
					.account(account)
					.date(LocalDateTime.now())
					.movementType(movement.getType())
					.amount(movement.getAmount())
					.balanceAfterMovement(newBalance)
					.build();
			accountRepository.save(account);
			MovementEntity saved = movementRepository.save(entity);
			log.info("Movement registered: accountNumber={}, type={}, amount={}, balanceAfter={}",
					accountNumber, movement.getType(), movement.getAmount(), newBalance);
			return movementEntityMapper.toDomain(saved);
		})).subscribeOn(Schedulers.boundedElastic());
	}

	@Override
	public Flux<Movement> getMovements(String accountNumber, LocalDate startDate, LocalDate endDate) {
		validateDateRange(startDate, endDate);
		LocalDateTime start = LocalDateTime.of(startDate, LocalTime.MIN);
		LocalDateTime end = LocalDateTime.of(endDate, LocalTime.MAX);
		return Mono.fromCallable(() -> {
			List<MovementEntity> entities;
			if (accountNumber != null && !accountNumber.isBlank()) {
				AccountEntity account = accountRepository.findByAccountNumberAndStatusTrue(accountNumber)
						.orElseThrow(() -> new AccountNotFoundException(
								"Account with number '" + accountNumber + "' not found"));
				entities = movementRepository.findByAccountAccountIdAndDateBetweenOrderByDateAsc(
						account.getAccountId(), start, end);
			} else {
				entities = movementRepository.findByDateBetweenOrderByDateAsc(start, end);
			}
			return entities;
		}).subscribeOn(Schedulers.boundedElastic())
				.flatMapMany(Flux::fromIterable)
				.map(movementEntityMapper::toDomain);
	}

	private void validateMovement(Movement movement) {
		if (movement == null) {
			throw new InvalidMovementException("Movement payload is required");
		}
		if (movement.getType() == null) {
			throw new InvalidMovementException("Movement type is required");
		}
		if (movement.getAmount() == null || movement.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
			throw new InvalidMovementException("Movement amount must be greater than zero");
		}
	}

	private BigDecimal calculateNewBalance(BigDecimal currentBalance, MovementType type, BigDecimal amount) {
		BigDecimal safeBalance = currentBalance != null ? currentBalance : BigDecimal.ZERO;
		if (type == MovementType.DEBIT) {
			if (safeBalance.compareTo(amount) < 0) {
				throw new InsufficientBalanceException("Saldo no disponible");
			}
			return safeBalance.subtract(amount);
		}
		return safeBalance.add(amount);
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
