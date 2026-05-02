package com.data.ntt.account_service.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.data.ntt.account_service.domain.enums.MovementType;

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
public class Movement {
	private Long id;
	private LocalDateTime date;
	private MovementType type;
	private BigDecimal amount;
	private BigDecimal balanceAfterMovement;
	private Long accountId;
	private Long version;
}
