package com.data.ntt.account_service.infrastructure.persistence.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.data.ntt.account_service.domain.enums.AccountType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "accounts")
public class AccountEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "account_id")
	private Long accountId;

	@Column(name = "account_number", nullable = false, unique = true, length = 10)
	private String accountNumber;

	@Enumerated(EnumType.STRING)
	@Column(name = "account_type", nullable = false, length = 10)
	private AccountType accountType;

	@Column(name = "initial_balance", nullable = false, precision = 15, scale = 2)
	private BigDecimal initialBalance;

	@Column(name = "available_balance", nullable = false, precision = 15, scale = 2)
	private BigDecimal availableBalance;

	@Column(name = "status", nullable = false)
	private Boolean status;

	@Column(name = "customer_id", nullable = false)
	private Long customerId;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@UpdateTimestamp
	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	@Version
	@Column(name = "version", nullable = false)
	private Long version;
}
