package com.data.ntt.account_service.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import com.data.ntt.account_service.application.service.impl.MovementServiceImpl;
import com.data.ntt.account_service.domain.enums.AccountType;
import com.data.ntt.account_service.domain.enums.MovementType;
import com.data.ntt.account_service.domain.model.Movement;
import com.data.ntt.account_service.infrastructure.persistence.entity.AccountEntity;
import com.data.ntt.account_service.infrastructure.persistence.entity.MovementEntity;
import com.data.ntt.account_service.infrastructure.persistence.mapper.MovementEntityMapper;
import com.data.ntt.account_service.infrastructure.persistence.repository.AccountRepository;
import com.data.ntt.account_service.infrastructure.persistence.repository.MovementRepository;
import com.data.ntt.account_service.shared.exception.InsufficientBalanceException;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class MovementServiceImplTest {
    private static final String ACCOUNT_NUMBER = "ACC0000001";

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private MovementRepository movementRepository;

    @Mock
    private TransactionTemplate transactionTemplate;

    private MovementEntityMapper movementEntityMapper;
    private MovementServiceImpl service;

    @BeforeEach
    void setUp() {
        movementEntityMapper = new MovementEntityMapper();
        service = new MovementServiceImpl(accountRepository, movementRepository, movementEntityMapper,
                transactionTemplate);
    }

    @Test
    void createMovementDebitInsufficientBalanceEmitsError() {
        mockTransactionTemplate();
        when(accountRepository.findByAccountNumberAndStatusTrue(ACCOUNT_NUMBER))
                .thenReturn(Optional.of(buildAccount(new BigDecimal("5.00"))));

        Movement movement = buildMovement(MovementType.DEBIT, new BigDecimal("10.00"));
        Mono<Movement> result = service.createMovement(ACCOUNT_NUMBER, movement);

        StepVerifier.create(result)
                .expectErrorSatisfies(error -> assertThat(error)
                        .isInstanceOf(InsufficientBalanceException.class))
                .verify();

        verify(accountRepository, never()).save(any(AccountEntity.class));
        verify(movementRepository, never()).save(any(MovementEntity.class));
    }

    @Test
    void createMovementDebitSuccessUpdatesBalanceAndReturnsMovement() {
        mockTransactionTemplate();
        AccountEntity account = buildAccount(new BigDecimal("100.00"));
        when(accountRepository.findByAccountNumberAndStatusTrue(ACCOUNT_NUMBER))
                .thenReturn(Optional.of(account));
        when(movementRepository.save(any(MovementEntity.class)))
                .thenAnswer(invocation -> {
                    MovementEntity entity = invocation.getArgument(0);
                    entity.setMovementId(1L);
                    entity.setVersion(1L);
                    return entity;
                });

        Movement movement = buildMovement(MovementType.DEBIT, new BigDecimal("25.00"));
        Mono<Movement> result = service.createMovement(ACCOUNT_NUMBER, movement);

        StepVerifier.create(result)
                .assertNext(saved -> {
                    assertThat(saved.getType()).isEqualTo(MovementType.DEBIT);
                    assertThat(saved.getAmount()).isEqualByComparingTo("25.00");
                    assertThat(saved.getBalanceAfterMovement()).isEqualByComparingTo("75.00");
                    assertThat(saved.getAccountId()).isEqualTo(1L);
                    assertThat(saved.getDate()).isNotNull();
                })
                .verifyComplete();

        ArgumentCaptor<AccountEntity> accountCaptor = ArgumentCaptor.forClass(AccountEntity.class);
        verify(accountRepository).save(accountCaptor.capture());
        assertThat(accountCaptor.getValue().getAvailableBalance()).isEqualByComparingTo("75.00");
    }

    private void mockTransactionTemplate() {
        when(transactionTemplate.execute(any())).thenAnswer(invocation -> {
            TransactionCallback<?> callback = invocation.getArgument(0);
            return callback.doInTransaction(null);
        });
    }

    private AccountEntity buildAccount(BigDecimal availableBalance) {
        BigDecimal initialBalance = availableBalance != null ? availableBalance : BigDecimal.ZERO;
        return AccountEntity.builder()
                .accountId(1L)
                .accountNumber(ACCOUNT_NUMBER)
                .accountType(AccountType.SAVINGS)
                .initialBalance(initialBalance)
                .availableBalance(availableBalance)
                .status(Boolean.TRUE)
                .customerId(99L)
                .build();
    }

    private Movement buildMovement(MovementType type, BigDecimal amount) {
        return Movement.builder()
                .type(type)
                .amount(amount)
                .build();
    }

}
