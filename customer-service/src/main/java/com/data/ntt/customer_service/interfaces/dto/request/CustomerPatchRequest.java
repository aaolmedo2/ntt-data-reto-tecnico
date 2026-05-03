package com.data.ntt.customer_service.interfaces.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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
public class CustomerPatchRequest {

    @Size(min = 2, max = 100)
    @Pattern(regexp = "^[A-Za-z ]+$", message = "Name must contain only letters and spaces")
    private String name;

    @Size(min = 5, max = 200)
    private String address;

    @Size(min = 10, max = 10)
    @Pattern(regexp = "^\\d{10}$")
    private String phone;

    @Size(min = 4, max = 100)
    private String password;

    private Boolean status;
}
