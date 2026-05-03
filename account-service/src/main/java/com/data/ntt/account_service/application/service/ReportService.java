package com.data.ntt.account_service.application.service;

import java.time.LocalDate;

import com.data.ntt.account_service.domain.model.AccountStatement;

import reactor.core.publisher.Mono;

public interface ReportService {
	Mono<AccountStatement> getAccountStatement(String customerIdentification, LocalDate startDate, LocalDate endDate);
}
