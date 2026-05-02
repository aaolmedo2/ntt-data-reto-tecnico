package com.data.ntt.account_service.infrastructure.persistence.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.data.ntt.account_service.infrastructure.persistence.entity.MovementEntity;

public interface MovementRepository extends JpaRepository<MovementEntity, Long> {
	List<MovementEntity> findByAccountAccountIdAndDateBetweenOrderByDateAsc(
			Long accountId,
			LocalDateTime start,
			LocalDateTime end);

	List<MovementEntity> findByDateBetweenOrderByDateAsc(LocalDateTime start, LocalDateTime end);
}
