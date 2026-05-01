package com.data.ntt.customer_service.interfaces.dto.response;

import com.data.ntt.customer_service.domain.enums.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerResponse {
    private String name;
    private Gender gender;
    private String identification;
    private String address;
    private String phone;
    private Boolean status;
}
