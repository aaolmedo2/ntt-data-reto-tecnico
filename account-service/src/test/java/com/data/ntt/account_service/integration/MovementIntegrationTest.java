package com.data.ntt.account_service.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.data.ntt.account_service.domain.enums.AccountType;
import com.data.ntt.account_service.domain.enums.MovementType;
import com.data.ntt.account_service.infrastructure.persistence.entity.AccountEntity;
import com.data.ntt.account_service.infrastructure.persistence.repository.AccountRepository;
import com.data.ntt.account_service.infrastructure.persistence.repository.MovementRepository;
import com.data.ntt.account_service.interfaces.dto.request.MovementRequest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class MovementIntegrationTest {
	private static final String ACCOUNT_NUMBER = "ACC0000001";

	@LocalServerPort
	private int port;

	private WebTestClient webTestClient;

	@Autowired
	private AccountRepository accountRepository;

	@Autowired
	private MovementRepository movementRepository;

	@BeforeEach
	void setUp() {
		movementRepository.deleteAll();
		accountRepository.deleteAll();
		webTestClient = WebTestClient.bindToServer()
				.baseUrl("http://localhost:" + port)
				.build();
	}

	@Test
	void createAndGetMovementsEndToEnd() {
		AccountEntity account = buildAccount(new BigDecimal("100.00"));
		accountRepository.save(account);

		MovementRequest request = MovementRequest.builder()
				.accountNumber(ACCOUNT_NUMBER)
				.type(MovementType.DEBIT)
				.amount(new BigDecimal("10.00"))
				.build();

		webTestClient.post()
				.uri("/api/v1/movements")
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue(request)
				.exchange()
				.expectStatus().isCreated()
				.expectBody()
				.jsonPath("$.type").isEqualTo("DEBIT")
				.jsonPath("$.amount").value(value -> {
					assertThat(new BigDecimal(value.toString()))
							.isEqualByComparingTo("10.00");
				})
				.jsonPath("$.balanceAfterMovement").value(value -> {
					assertThat(new BigDecimal(value.toString()))
							.isEqualByComparingTo("90.00");
				});

		LocalDate startDate = LocalDate.now().minusDays(1);
		LocalDate endDate = LocalDate.now().plusDays(1);

		webTestClient.get()
				.uri(uriBuilder -> uriBuilder
						.path("/api/v1/movements")
						.queryParam("startDate", startDate)
						.queryParam("endDate", endDate)
						.build())
				.exchange()
				.expectStatus().isOk()
				.expectBody()
				.jsonPath("$[0].type").isEqualTo("DEBIT")
				.jsonPath("$[0].amount").value(value -> {
					assertThat(new BigDecimal(value.toString()))
							.isEqualByComparingTo("10.00");
				})
				.jsonPath("$[0].balanceAfterMovement").value(value -> {
					assertThat(new BigDecimal(value.toString()))
							.isEqualByComparingTo("90.00");
				})
				.jsonPath("$[0].account").doesNotExist();
	}

	private AccountEntity buildAccount(BigDecimal availableBalance) {
		return AccountEntity.builder()
				.accountNumber(ACCOUNT_NUMBER)
				.accountType(AccountType.SAVINGS)
				.initialBalance(availableBalance)
				.availableBalance(availableBalance)
				.status(Boolean.TRUE)
				.customerId(101L)
				.build();
	}
}
