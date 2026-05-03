package com.data.ntt.account_service.application.service;

import com.data.ntt.account_service.domain.model.Account;

import reactor.core.publisher.Mono;

public interface AccountService {
	Mono<Account> createAccount(Account account, String customerIdentification);

	Mono<Account> getAccountByNumber(String accountNumber);

	Mono<Account> updateAccount(String accountNumber, Account accountPatch);

	Mono<Void> deleteAccount(String accountNumber);
}
