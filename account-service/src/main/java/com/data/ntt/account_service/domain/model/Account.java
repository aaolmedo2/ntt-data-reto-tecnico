package com.data.ntt.account_service.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.data.ntt.account_service.domain.enums.AccountType;

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
public class Account {
	private Long id;
	private String accountNumber;
	private AccountType accountType;
	private BigDecimal initialBalance;
	private BigDecimal availableBalance;
	private Boolean status;
	private Long customerId;
	private String customerIdentification;
	private String customerName;
	private String customerGender;
	private String customerAddress;
	private String customerPhone;
	private Boolean customerStatus;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	private Long version;
}
