package com.data.ntt.account_service.interfaces.controller;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.data.ntt.account_service.application.service.AccountService;
import com.data.ntt.account_service.application.service.MovementService;
import com.data.ntt.account_service.interfaces.dto.mapper.AccountMapper;
import com.data.ntt.account_service.interfaces.dto.mapper.MovementMapper;
import com.data.ntt.account_service.interfaces.dto.request.MovementRequest;
import com.data.ntt.account_service.interfaces.dto.response.MovementResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/movements")
public class MovementController {
	private final AccountService accountService;
	private final MovementService movementService;
	private final AccountMapper accountMapper;
	private final MovementMapper movementMapper;

	@GetMapping
	public Flux<MovementResponse> getMovements(
			@RequestParam(required = false) String accountNumber,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
		if (accountNumber == null || accountNumber.isBlank()) {
			return movementService.getMovements(null, startDate, endDate)
					.map(movementMapper::toResponse);
		}
		return accountService.getAccountByNumber(accountNumber)
				.map(accountMapper::toResponse)
				.flatMapMany(account -> movementService.getMovements(accountNumber, startDate, endDate)
						.map(movement -> movementMapper.toResponse(movement, account)));
	}

	@PostMapping
	public Mono<ResponseEntity<MovementResponse>> createMovement(@Valid @RequestBody MovementRequest request) {
		return accountService.getAccountByNumber(request.getAccountNumber())
				.map(accountMapper::toResponse)
				.flatMap(account -> movementService.createMovement(request.getAccountNumber(),
						movementMapper.toDomain(request))
						.map(movement -> movementMapper.toResponse(movement, account)))
				.map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response));
	}
}
