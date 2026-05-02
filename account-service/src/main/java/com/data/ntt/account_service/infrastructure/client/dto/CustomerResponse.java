package com.data.ntt.account_service.infrastructure.client.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

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
public class CustomerResponse {
	@JsonAlias({ "id", "customerId" })
	private Long id;

	@JsonAlias({ "name", "fullName" })
	private String name;

	@JsonAlias({ "identification", "identificationNumber" })
	private String identification;

	@JsonAlias({ "gender" })
	private String gender;

	@JsonAlias({ "address" })
	private String address;

	@JsonAlias({ "phone", "phoneNumber" })
	private String phone;

	private Boolean status;
}
