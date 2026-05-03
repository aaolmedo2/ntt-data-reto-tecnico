package com.data.ntt.account_service.infrastructure.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import com.data.ntt.account_service.infrastructure.client.CustomerServiceProperties;

@Configuration
@EnableConfigurationProperties(CustomerServiceProperties.class)
public class CustomerServiceConfig {
	@Bean
	public WebClient customerWebClient(CustomerServiceProperties properties) {
		return WebClient.builder()
				.baseUrl(properties.getBaseUrl())
				.build();
	}
}
