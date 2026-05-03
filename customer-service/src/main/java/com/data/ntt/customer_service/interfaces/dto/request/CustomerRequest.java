package com.data.ntt.customer_service.interfaces.dto.request;

import com.data.ntt.customer_service.domain.enums.Gender;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class CustomerRequest {

    @NotBlank
    @Size(min = 2, max = 100)
    @Pattern(regexp = "^[A-Za-z ]+$", message = "Name must contain only letters and spaces")
    private String name;

    @NotNull
    private Gender gender;

    @NotBlank
    @Size(min = 10, max = 10)
    @Pattern(regexp = "^\\d{10}$")
    private String identification;

    @NotBlank
    @Size(min = 5, max = 200)
    private String address;

    @NotBlank
    @Size(min = 10, max = 10)
    @Pattern(regexp = "^\\d{10}$")
    private String phone;

    @NotBlank
    @Size(min = 4, max = 100)
    private String password;
}
