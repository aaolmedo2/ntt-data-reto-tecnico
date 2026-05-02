package com.data.ntt.account_service.infrastructure.client;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "customer-service")
public class CustomerServiceProperties {
	@NotBlank
	private String baseUrl;
}
