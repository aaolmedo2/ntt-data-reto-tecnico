package com.data.ntt.account_service.application.service.impl;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import com.data.ntt.account_service.application.service.AccountService;
import com.data.ntt.account_service.domain.model.Account;
import com.data.ntt.account_service.infrastructure.client.CustomerServiceClient;
import com.data.ntt.account_service.infrastructure.client.dto.CustomerResponse;
import com.data.ntt.account_service.infrastructure.persistence.entity.AccountEntity;
import com.data.ntt.account_service.infrastructure.persistence.mapper.AccountEntityMapper;
import com.data.ntt.account_service.infrastructure.persistence.repository.AccountRepository;
import com.data.ntt.account_service.shared.exception.AccountAlreadyExistsException;
import com.data.ntt.account_service.shared.exception.AccountNotFoundException;
import com.data.ntt.account_service.shared.exception.BadRequestException;
import com.data.ntt.account_service.shared.util.CustomerIdParser;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {
	private final AccountRepository accountRepository;
	private final AccountEntityMapper accountEntityMapper;
	private final CustomerServiceClient customerServiceClient;
	private final TransactionTemplate transactionTemplate;

	@Override
	public Mono<Account> createAccount(Account account, String customerIdentification) {
		if (account == null) {
			return Mono.error(new BadRequestException("Account payload is required"));
		}
		if (account.getAccountNumber() == null || account.getAccountNumber().isBlank()) {
			return Mono.error(new BadRequestException("accountNumber is required"));
		}
		return customerServiceClient.getCustomerByIdentification(customerIdentification)
				.flatMap(customer -> Mono.fromCallable(() -> transactionTemplate.execute(status -> {
					String accountNumber = account.getAccountNumber();
					if (accountRepository.existsByAccountNumber(accountNumber)) {
						throw new AccountAlreadyExistsException(
								"Account number '" + accountNumber + "' already exists");
					}
					Long customerId = customer.getId() != null
							? customer.getId()
							: CustomerIdParser.parseCustomerId(customerIdentification);
					AccountEntity entity = accountEntityMapper.toEntity(account);
					entity.setAccountNumber(accountNumber);
					BigDecimal initialBalance = entity.getInitialBalance() != null
							? entity.getInitialBalance()
							: BigDecimal.ZERO;
					entity.setInitialBalance(initialBalance);
					entity.setAvailableBalance(initialBalance);
					entity.setCustomerId(customerId);
					entity.setStatus(Boolean.TRUE);
					AccountEntity saved = accountRepository.save(entity);
					log.info("Account created: accountNumber={}, customerId={}", saved.getAccountNumber(),
							saved.getCustomerId());
					Account savedDomain = accountEntityMapper.toDomain(saved);
					return enrichAccountWithCustomer(savedDomain, customer);
				})).subscribeOn(Schedulers.boundedElastic()));
	}

	@Override
	public Mono<Account> getAccountByNumber(String accountNumber) {
		return Mono.fromCallable(() -> accountRepository.findByAccountNumberAndStatusTrue(accountNumber)
				.orElseThrow(() -> new AccountNotFoundException(
						"Account with number '" + accountNumber + "' not found")))
				.subscribeOn(Schedulers.boundedElastic())
				.map(accountEntityMapper::toDomain)
				.flatMap(this::enrichAccountWithCustomer);
	}

	@Override
	public Mono<Account> updateAccount(String accountNumber, Account accountPatch) {
		return Mono.fromCallable(() -> transactionTemplate.execute(status -> {
			AccountEntity entity = accountRepository.findByAccountNumberAndStatusTrue(accountNumber)
					.orElseThrow(() -> new AccountNotFoundException(
							"Account with number '" + accountNumber + "' not found"));
			if (accountPatch != null) {
				if (accountPatch.getAccountType() != null) {
					entity.setAccountType(accountPatch.getAccountType());
				}
				if (accountPatch.getStatus() != null) {
					entity.setStatus(accountPatch.getStatus());
				}
			}
			AccountEntity saved = accountRepository.save(entity);
			log.info("Account updated: accountNumber={}", saved.getAccountNumber());
			return accountEntityMapper.toDomain(saved);
		})).subscribeOn(Schedulers.boundedElastic());
	}

	@Override
	public Mono<Void> deleteAccount(String accountNumber) {
		return Mono.fromRunnable(() -> transactionTemplate.executeWithoutResult(status -> {
			AccountEntity entity = accountRepository.findByAccountNumberAndStatusTrue(accountNumber)
					.orElseThrow(() -> new AccountNotFoundException(
							"Account with number '" + accountNumber + "' not found"));
			entity.setStatus(Boolean.FALSE);
			accountRepository.save(entity);
			log.info("Account deactivated: accountNumber={}", entity.getAccountNumber());
		})).subscribeOn(Schedulers.boundedElastic()).then();
	}

	private Mono<Account> enrichAccountWithCustomer(Account account) {
		if (account == null) {
			return Mono.just(account);
		}
		String identification = account.getCustomerIdentification();
		if ((identification == null || identification.isBlank()) && account.getCustomerId() != null) {
			identification = account.getCustomerId().toString();
		}
		if (identification == null || identification.isBlank()) {
			return Mono.just(account);
		}
		return customerServiceClient.getCustomerByIdentification(identification)
				.map(customer -> enrichAccountWithCustomer(account, customer));
	}

	private Account enrichAccountWithCustomer(Account account, CustomerResponse customer) {
		if (account == null || customer == null) {
			return account;
		}
		if (customer.getId() != null) {
			account.setCustomerId(customer.getId());
		}
		if (customer.getIdentification() != null && !customer.getIdentification().isBlank()) {
			account.setCustomerIdentification(customer.getIdentification());
		} else if (account.getCustomerId() != null) {
			account.setCustomerIdentification(account.getCustomerId().toString());
		}
		account.setCustomerName(customer.getName());
		account.setCustomerGender(customer.getGender());
		account.setCustomerAddress(customer.getAddress());
		account.setCustomerPhone(customer.getPhone());
		account.setCustomerStatus(customer.getStatus());
		return account;
	}
}
