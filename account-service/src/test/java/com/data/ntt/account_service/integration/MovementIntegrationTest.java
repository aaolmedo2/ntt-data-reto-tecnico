package com.data.ntt.account_service.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.data.ntt.account_service.domain.enums.AccountType;
import com.data.ntt.account_service.domain.enums.MovementType;
import com.data.ntt.account_service.infrastructure.client.CustomerServiceClient;
import com.data.ntt.account_service.infrastructure.client.dto.CustomerResponse;
import com.data.ntt.account_service.infrastructure.persistence.entity.AccountEntity;
import com.data.ntt.account_service.infrastructure.persistence.entity.MovementEntity;
import com.data.ntt.account_service.infrastructure.persistence.repository.AccountRepository;
import com.data.ntt.account_service.infrastructure.persistence.repository.MovementRepository;
import com.data.ntt.account_service.interfaces.dto.request.MovementRequest;

import reactor.core.publisher.Mono;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class MovementIntegrationTest {

        private static final String ACCOUNT_NUMBER = "ACC0000001";
        private static final String CUSTOMER_IDENTIFICATION = "1234567890";
        private static final Long CUSTOMER_ID = 101L;

        @LocalServerPort
        private int port;

        private WebTestClient webTestClient;

        @Autowired
        private AccountRepository accountRepository;

        @Autowired
        private MovementRepository movementRepository;

        /**
         * El MS de clientes se mockea porque los endpoints de movimientos y reporte
         * llaman a customerServiceClient internamente para enriquecer la respuesta.
         * Sin este mock, el servidor responde 500 al no encontrar al cliente.
         */
        @MockitoBean
        private CustomerServiceClient customerServiceClient;

        @BeforeEach
        void setUp() {
                movementRepository.deleteAll();
                accountRepository.deleteAll();

                CustomerResponse fakeCustomer = CustomerResponse.builder()
                                .id(CUSTOMER_ID)
                                .identification(CUSTOMER_IDENTIFICATION)
                                .name("Test User")
                                .status(Boolean.TRUE)
                                .build();

                when(customerServiceClient.getCustomerByIdentification(anyString()))
                                .thenReturn(Mono.just(fakeCustomer));

                webTestClient = WebTestClient.bindToServer()
                                .baseUrl("http://localhost:" + port)
                                .build();
        }

        // ---------------------------------------------------------------
        // Crear cuenta: se persiste directo al repo y se verifica en DB
        // No se usa POST /api/v1/accounts para evitar la llamada al MS de clientes
        // ---------------------------------------------------------------
        @Test
        void createAccountSavesToDatabase() {
                accountRepository.save(buildAccount(new BigDecimal("500.00")));

                assertThat(accountRepository.findByAccountNumberAndStatusTrue(ACCOUNT_NUMBER))
                                .isPresent()
                                .hasValueSatisfying(saved -> {
                                        assertThat(saved.getAccountNumber()).isEqualTo(ACCOUNT_NUMBER);
                                        assertThat(saved.getAccountType()).isEqualTo(AccountType.SAVINGS);
                                        assertThat(saved.getAvailableBalance()).isEqualByComparingTo("500.00");
                                        assertThat(saved.getStatus()).isTrue();
                                        assertThat(saved.getCustomerId()).isEqualTo(CUSTOMER_ID);
                                });
        }

        // ---------------------------------------------------------------
        // Registrar movimiento real:
        // 1. Cuenta insertada directo al repo
        // 2. POST /api/v1/movements
        // 3. Verificar en DB: movimiento guardado + saldo actualizado
        // ---------------------------------------------------------------
        @Test
        void createMovementPersistsMovementAndUpdatesAccountBalance() {
                accountRepository.save(buildAccount(new BigDecimal("300.00")));

                MovementRequest request = MovementRequest.builder()
                                .accountNumber(ACCOUNT_NUMBER)
                                .type(MovementType.DEBIT)
                                .amount(new BigDecimal("80.00"))
                                .build();

                webTestClient.post()
                                .uri("/api/v1/movements")
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(request)
                                .exchange()
                                .expectStatus().isCreated()
                                .expectBody()
                                .jsonPath("$.type").isEqualTo("DEBIT")
                                .jsonPath("$.amount").value(value -> assertThat(new BigDecimal(value.toString()))
                                                .isEqualByComparingTo("80.00"))
                                .jsonPath("$.balanceAfterMovement")
                                .value(value -> assertThat(new BigDecimal(value.toString()))
                                                .isEqualByComparingTo("220.00"));

                // Movimiento guardado en DB
                List<MovementEntity> movements = movementRepository.findAll();
                assertThat(movements).hasSize(1);
                MovementEntity saved = movements.get(0);
                assertThat(saved.getAmount()).isEqualByComparingTo("80.00");
                assertThat(saved.getBalanceAfterMovement()).isEqualByComparingTo("220.00");

                // Saldo de la cuenta actualizado en DB
                AccountEntity updated = accountRepository.findByAccountNumberAndStatusTrue(ACCOUNT_NUMBER)
                                .orElseThrow();
                assertThat(updated.getAvailableBalance()).isEqualByComparingTo("220.00");
        }

        // ---------------------------------------------------------------
        // Endpoint de reporte: GET /reports/{identification}?startDate=&endDate=
        // Respuesta: archivo Excel (.xlsx) descargable
        // → Content-Type:
        // application/vnd.openxmlformats-officedocument.spreadsheetml.sheet
        // → Content-Disposition: attachment
        // → body con firma PK (ZIP) válida de un .xlsx
        // ---------------------------------------------------------------
        @Test
        void getReportReturnsExcelFile() {
                accountRepository.save(buildAccount(new BigDecimal("1000.00")));

                // Crédito de 200 → saldo queda 1200
                webTestClient.post()
                                .uri("/api/v1/movements")
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(MovementRequest.builder()
                                                .accountNumber(ACCOUNT_NUMBER)
                                                .type(MovementType.CREDIT)
                                                .amount(new BigDecimal("200.00"))
                                                .build())
                                .exchange()
                                .expectStatus().isCreated();

                // Débito de 50 → saldo queda 1150
                webTestClient.post()
                                .uri("/api/v1/movements")
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(MovementRequest.builder()
                                                .accountNumber(ACCOUNT_NUMBER)
                                                .type(MovementType.DEBIT)
                                                .amount(new BigDecimal("50.00"))
                                                .build())
                                .exchange()
                                .expectStatus().isCreated();

                LocalDate startDate = LocalDate.now().minusDays(2);
                LocalDate endDate = LocalDate.now();

                webTestClient.get()
                                .uri(uriBuilder -> uriBuilder
                                                .path("/reports/{identification}")
                                                .queryParam("startDate", startDate)
                                                .queryParam("endDate", endDate)
                                                .build(CUSTOMER_IDENTIFICATION))
                                .exchange()
                                .expectStatus().isOk()
                                .expectHeader()
                                .contentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                                .expectHeader().valueMatches("Content-Disposition", ".*attachment.*")
                                .expectBody(byte[].class)
                                .consumeWith(response -> {
                                        byte[] body = response.getResponseBody();
                                        assertThat(body).isNotNull().isNotEmpty();
                                        // Los archivos .xlsx son ZIPs: comienzan con la firma PK (0x50 0x4B)
                                        assertThat(body[0]).isEqualTo((byte) 0x50); // 'P'
                                        assertThat(body[1]).isEqualTo((byte) 0x4B); // 'K'
                                });
        }

        // ---------------------------------------------------------------
        // Helper
        // ---------------------------------------------------------------
        private AccountEntity buildAccount(BigDecimal availableBalance) {
                return AccountEntity.builder()
                                .accountNumber(ACCOUNT_NUMBER)
                                .accountType(AccountType.SAVINGS)
                                .initialBalance(availableBalance)
                                .availableBalance(availableBalance)
                                .status(Boolean.TRUE)
                                .customerId(CUSTOMER_ID)
                                .build();
        }
}