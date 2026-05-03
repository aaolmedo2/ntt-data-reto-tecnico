package com.data.ntt.account_service.infrastructure.persistence.mapper;

import org.springframework.stereotype.Component;

import com.data.ntt.account_service.domain.model.Movement;
import com.data.ntt.account_service.infrastructure.persistence.entity.MovementEntity;

@Component
public class MovementEntityMapper {
	public Movement toDomain(MovementEntity entity) {
		if (entity == null) {
			return null;
		}
		Long accountId = entity.getAccount() != null ? entity.getAccount().getAccountId() : null;
		return Movement.builder()
				.id(entity.getMovementId())
				.date(entity.getDate())
				.type(entity.getMovementType())
				.amount(entity.getAmount())
				.balanceAfterMovement(entity.getBalanceAfterMovement())
				.accountId(accountId)
				.version(entity.getVersion())
				.build();
	}
}
