package com.data.ntt.account_service.application.service;

import java.time.LocalDate;

import com.data.ntt.account_service.domain.model.Movement;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface MovementService {
	Mono<Movement> createMovement(String accountNumber, Movement movement);

	Flux<Movement> getMovements(String accountNumber, LocalDate startDate, LocalDate endDate);
}
