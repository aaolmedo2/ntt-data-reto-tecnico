package com.data.ntt.account_service.infrastructure.persistence.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.data.ntt.account_service.domain.enums.MovementType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
@Table(name = "movements")
public class MovementEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "movement_id")
	private Long movementId;

	@Column(name = "date", nullable = false)
	private LocalDateTime date;

	@Enumerated(EnumType.STRING)
	@Column(name = "movement_type", nullable = false, length = 10)
	private MovementType movementType;

	@Column(name = "amount", nullable = false, precision = 15, scale = 2)
	private BigDecimal amount;

	@Column(name = "balance_after_movement", nullable = false, precision = 15, scale = 2)
	private BigDecimal balanceAfterMovement;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "account_id", nullable = false)
	private AccountEntity account;

	@Version
	@Column(name = "version", nullable = false)
	private Long version;
}
