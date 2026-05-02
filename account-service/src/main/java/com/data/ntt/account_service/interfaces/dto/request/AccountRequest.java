package com.data.ntt.account_service.interfaces.dto.request;

import java.math.BigDecimal;

import com.data.ntt.account_service.domain.enums.AccountType;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
public class AccountRequest {
	@NotBlank
	@Size(min = 10, max = 10)
	private String accountNumber;

	@NotNull
	private AccountType accountType;

	@NotNull
	@DecimalMin("0.00")
	private BigDecimal initialBalance;

	@NotBlank
	private String customerIdentification;
}
