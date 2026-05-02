package com.data.ntt.account_service.interfaces.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.data.ntt.account_service.application.service.AccountService;
import com.data.ntt.account_service.domain.model.Account;
import com.data.ntt.account_service.interfaces.dto.mapper.AccountMapper;
import com.data.ntt.account_service.interfaces.dto.request.AccountPatchRequest;
import com.data.ntt.account_service.interfaces.dto.request.AccountRequest;
import com.data.ntt.account_service.interfaces.dto.response.AccountResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/accounts")
public class AccountController {
	private final AccountService accountService;
	private final AccountMapper accountMapper;

	@PostMapping
	public Mono<ResponseEntity<AccountResponse>> createAccount(@Valid @RequestBody AccountRequest request) {
		Account account = accountMapper.toDomain(request);
		return accountService.createAccount(account, request.getCustomerIdentification())
				.map(accountMapper::toResponse)
				.map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response));
	}

	@GetMapping("/{accountNumber}")
	public Mono<AccountResponse> getAccountByAccountNumber(@PathVariable String accountNumber) {
		return accountService.getAccountByNumber(accountNumber)
				.map(accountMapper::toResponse);
	}

	@PatchMapping("/{accountNumber}")
	public Mono<AccountResponse> updateAccountByAccountNumber(
			@PathVariable String accountNumber,
			@Valid @RequestBody AccountPatchRequest request) {
		Account patch = accountMapper.toPatchDomain(request);
		return accountService.updateAccount(accountNumber, patch)
				.map(accountMapper::toResponse);
	}

	@DeleteMapping("/{accountNumber}")
	public Mono<ResponseEntity<Void>> deleteAccountByAccountNumber(@PathVariable String accountNumber) {
		return accountService.deleteAccount(accountNumber)
				.thenReturn(ResponseEntity.noContent().build());
	}
}
