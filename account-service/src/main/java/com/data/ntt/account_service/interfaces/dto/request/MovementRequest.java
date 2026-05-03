package com.data.ntt.account_service.interfaces.dto.request;

import java.math.BigDecimal;

import com.data.ntt.account_service.domain.enums.MovementType;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class MovementRequest {
	@NotBlank
	private String accountNumber;

	@NotNull
	private MovementType type;

	@NotNull
	@DecimalMin("0.01")
	private BigDecimal amount;
}
