package com.data.ntt.account_service.interfaces.dto.request;

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
public class AccountPatchRequest {
	private AccountType accountType;
	private Boolean status;
}
