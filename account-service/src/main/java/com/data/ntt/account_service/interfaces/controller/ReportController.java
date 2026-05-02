package com.data.ntt.account_service.interfaces.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.server.reactive.ServerHttpRequest;

import com.data.ntt.account_service.application.service.ReportService;
import com.data.ntt.account_service.interfaces.dto.mapper.AccountStatementExcelExporter;
import com.data.ntt.account_service.interfaces.dto.mapper.ReportMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/reports")
public class ReportController {
	private static final MediaType EXCEL_MEDIA_TYPE = MediaType.parseMediaType(
			"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
	private static final MediaType LEGACY_EXCEL_MEDIA_TYPE = MediaType.parseMediaType("application/vnd.ms-excel");

	private final ReportService reportService;
	private final ReportMapper reportMapper;
	private final AccountStatementExcelExporter excelExporter;

	@GetMapping("/{identification}")
	public Mono<ResponseEntity<?>> getAccountStatementByCustomerIdentification(
			@PathVariable("identification") String identification,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
			ServerHttpRequest request) {
		return reportService.getAccountStatement(identification, startDate, endDate)
				.map(statement -> {
					if (acceptsExcel(request.getHeaders().getAccept())) {
						byte[] content = excelExporter.export(statement);
						return ResponseEntity.ok()
								.contentType(EXCEL_MEDIA_TYPE)
								.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=account-statement.xlsx")
								.body(content);
					}
					return ResponseEntity.ok()
							.contentType(MediaType.APPLICATION_JSON)
							.body(reportMapper.toResponse(statement));
				});
	}

	private boolean acceptsExcel(List<MediaType> accepted) {
		if (accepted == null || accepted.isEmpty()) {
			return false;
		}
		return accepted.stream().anyMatch(mediaType -> mediaType.isCompatibleWith(EXCEL_MEDIA_TYPE)
				|| mediaType.isCompatibleWith(LEGACY_EXCEL_MEDIA_TYPE));
	}
}
