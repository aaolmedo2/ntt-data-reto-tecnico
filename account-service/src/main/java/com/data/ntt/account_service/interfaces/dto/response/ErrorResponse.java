package com.data.ntt.account_service.interfaces.dto.response;

import java.time.Instant;

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
public class ErrorResponse {
	private Instant timestamp;
	private int status;
	private String error;
	private String message;
	private String path;
}
