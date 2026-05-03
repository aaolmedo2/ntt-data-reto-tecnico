package com.data.ntt.account_service.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.data.ntt.account_service.infrastructure.persistence.entity.AccountEntity;

public interface AccountRepository extends JpaRepository<AccountEntity, Long> {
	Optional<AccountEntity> findByAccountNumber(String accountNumber);

	Optional<AccountEntity> findByAccountNumberAndStatusTrue(String accountNumber);

	boolean existsByAccountNumber(String accountNumber);

	List<AccountEntity> findByCustomerIdAndStatusTrue(Long customerId);
}
