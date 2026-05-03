package com.data.ntt.account_service.interfaces.dto.mapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import com.data.ntt.account_service.domain.model.AccountStatement;
import com.data.ntt.account_service.domain.model.AccountStatementDetail;
import com.data.ntt.account_service.domain.model.Movement;

@Component
public class AccountStatementExcelExporter {
	private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;
	private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
	private static final ZoneId UTC = ZoneId.of("UTC");
	private static final ZoneId ECUADOR_ZONE = ZoneId.of("America/Guayaquil"); // UTC-5

	private LocalDateTime toEcuadorTime(LocalDateTime utcDateTime) {
		if (utcDateTime == null)
			return null;
		return utcDateTime
				.atZone(UTC)
				.withZoneSameInstant(ECUADOR_ZONE)
				.toLocalDateTime();
	}

	public byte[] export(AccountStatement statement) {
		try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
			Sheet sheet = workbook.createSheet("Account Statement");
			int rowIndex = 0;

			Row customerRow = sheet.createRow(rowIndex++);
			customerRow.createCell(0).setCellValue("Customer");
			customerRow.createCell(1).setCellValue(nullSafe(statement.getCustomerName()));

			Row identificationRow = sheet.createRow(rowIndex++);
			identificationRow.createCell(0).setCellValue("Identification");
			identificationRow.createCell(1).setCellValue(nullSafe(statement.getCustomerIdentification()));

			Row dateRow = sheet.createRow(rowIndex++);
			dateRow.createCell(0).setCellValue("StartDate");
			dateRow.createCell(1)
					.setCellValue(statement.getStartDate() != null ? statement.getStartDate().format(DATE_FORMAT) : "");
			dateRow.createCell(2).setCellValue("EndDate");
			dateRow.createCell(3)
					.setCellValue(statement.getEndDate() != null ? statement.getEndDate().format(DATE_FORMAT) : "");

			rowIndex++;
			Row header = sheet.createRow(rowIndex++);
			String[] headers = {
					"AccountNumber",
					"AccountType",
					"InitialBalance",
					"AvailableBalance",
					"Status",
					"MovementDate",
					"MovementType",
					"MovementAmount",
					"BalanceAfterMovement"
			};
			for (int i = 0; i < headers.length; i++) {
				header.createCell(i).setCellValue(headers[i]);
			}

			List<AccountStatementDetail> accounts = statement.getAccounts() != null ? statement.getAccounts()
					: List.of();
			for (AccountStatementDetail account : accounts) {
				List<Movement> movements = account.getMovements() != null ? account.getMovements() : List.of();
				if (movements.isEmpty()) {
					Row row = sheet.createRow(rowIndex++);
					writeAccountRow(row, account, null);
				} else {
					for (Movement movement : movements) {
						Row row = sheet.createRow(rowIndex++);
						writeAccountRow(row, account, movement);
					}
				}
			}

			workbook.write(output);
			return output.toByteArray();
		} catch (IOException ex) {
			throw new IllegalStateException("Failed to generate Excel report", ex);
		}
	}

	private void writeAccountRow(Row row, AccountStatementDetail account, Movement movement) {
		row.createCell(0).setCellValue(nullSafe(account.getAccountNumber()));
		row.createCell(1).setCellValue(account.getAccountType() != null ? account.getAccountType().name() : "");
		row.createCell(2)
				.setCellValue(account.getInitialBalance() != null ? account.getInitialBalance().doubleValue() : 0.0);
		row.createCell(3).setCellValue(
				account.getAvailableBalance() != null ? account.getAvailableBalance().doubleValue() : 0.0);
		row.createCell(4).setCellValue(account.getStatus() != null && account.getStatus());

		if (movement != null) {
			LocalDateTime ecuadorDate = toEcuadorTime(movement.getDate());
			row.createCell(5)
					.setCellValue(ecuadorDate != null ? ecuadorDate.format(DATE_TIME_FORMAT) : "");
			row.createCell(6).setCellValue(movement.getType() != null ? movement.getType().name() : "");
			row.createCell(7).setCellValue(movement.getAmount() != null ? movement.getAmount().doubleValue() : 0.0);
			row.createCell(8).setCellValue(movement.getBalanceAfterMovement() != null
					? movement.getBalanceAfterMovement().doubleValue()
					: 0.0);
		} else {
			row.createCell(5).setCellValue("");
			row.createCell(6).setCellValue("");
			row.createCell(7).setCellValue("");
			row.createCell(8).setCellValue("");
		}
	}

	private String nullSafe(String value) {
		return value == null ? "" : value;
	}
}