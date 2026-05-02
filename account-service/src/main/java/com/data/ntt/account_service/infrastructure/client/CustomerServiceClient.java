package com.data.ntt.account_service.infrastructure.client;

import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.data.ntt.account_service.infrastructure.client.dto.CustomerResponse;
import com.data.ntt.account_service.shared.exception.CustomerNotFoundException;
import com.data.ntt.account_service.shared.exception.CustomerServiceException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomerServiceClient {
	private final WebClient customerWebClient;

	public Mono<CustomerResponse> getCustomerByIdentification(String identification) {
		return customerWebClient.get()
				.uri(uriBuilder -> uriBuilder.path("/api/v1/customers/{identification}").build(identification))
				.retrieve()
				.onStatus(status -> status.value() == 404,
						response -> Mono.error(new CustomerNotFoundException(
								"Customer with identification " + identification + " not found")))
				.onStatus(HttpStatusCode::isError,
						response -> response.bodyToMono(String.class)
								.defaultIfEmpty("")
								.flatMap(body -> {
									log.warn("Customer service error: status={}, body={}", response.statusCode(), body);
									return Mono.error(new CustomerServiceException(
											"Customer service error: " + response.statusCode().value()));
								}))
				.bodyToMono(CustomerResponse.class);
	}
}
