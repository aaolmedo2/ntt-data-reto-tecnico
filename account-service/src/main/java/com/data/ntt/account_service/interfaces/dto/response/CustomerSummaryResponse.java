package com.data.ntt.account_service.interfaces.dto.response;

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
public class CustomerSummaryResponse {
    private String name;
    private String gender;
    private String identification;
    private String address;
    private String phone;
    private Boolean status;
}
