package com.data.ntt.account_service.interfaces.dto.mapper;

import org.springframework.stereotype.Component;

import com.data.ntt.account_service.domain.model.Movement;
import com.data.ntt.account_service.interfaces.dto.request.MovementRequest;
import com.data.ntt.account_service.interfaces.dto.response.AccountResponse;
import com.data.ntt.account_service.interfaces.dto.response.MovementResponse;

@Component
public class MovementMapper {
	public Movement toDomain(MovementRequest request) {
		if (request == null) {
			return null;
		}
		return Movement.builder()
				.type(request.getType())
				.amount(request.getAmount())
				.build();
	}

	public MovementResponse toResponse(Movement movement) {
		if (movement == null) {
			return null;
		}
		return MovementResponse.builder()
				.date(movement.getDate())
				.type(movement.getType())
				.amount(movement.getAmount())
				.balanceAfterMovement(movement.getBalanceAfterMovement())
				.build();
	}

	public MovementResponse toResponse(Movement movement, AccountResponse account) {
		MovementResponse response = toResponse(movement);
		if (response == null) {
			return null;
		}
		response.setAccount(account);
		return response;
	}
}
