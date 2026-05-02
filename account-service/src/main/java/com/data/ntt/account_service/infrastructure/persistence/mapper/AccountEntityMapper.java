package com.data.ntt.account_service.infrastructure.persistence.mapper;

import org.springframework.stereotype.Component;

import com.data.ntt.account_service.domain.model.Account;
import com.data.ntt.account_service.infrastructure.persistence.entity.AccountEntity;

@Component
public class AccountEntityMapper {
	public Account toDomain(AccountEntity entity) {
		if (entity == null) {
			return null;
		}
		return Account.builder()
				.id(entity.getAccountId())
				.accountNumber(entity.getAccountNumber())
				.accountType(entity.getAccountType())
				.initialBalance(entity.getInitialBalance())
				.availableBalance(entity.getAvailableBalance())
				.status(entity.getStatus())
				.customerId(entity.getCustomerId())
				.createdAt(entity.getCreatedAt())
				.updatedAt(entity.getUpdatedAt())
				.version(entity.getVersion())
				.build();
	}

	public AccountEntity toEntity(Account domain) {
		if (domain == null) {
			return null;
		}
		return AccountEntity.builder()
				.accountId(domain.getId())
				.accountNumber(domain.getAccountNumber())
				.accountType(domain.getAccountType())
				.initialBalance(domain.getInitialBalance())
				.availableBalance(domain.getAvailableBalance())
				.status(domain.getStatus())
				.customerId(domain.getCustomerId())
				.build();
	}
}
