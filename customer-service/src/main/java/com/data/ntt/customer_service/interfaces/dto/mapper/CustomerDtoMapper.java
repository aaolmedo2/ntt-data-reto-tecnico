package com.data.ntt.customer_service.interfaces.dto.mapper;

import com.data.ntt.customer_service.domain.model.Customer;
import com.data.ntt.customer_service.domain.model.Person;
import com.data.ntt.customer_service.interfaces.dto.request.CustomerPatchRequest;
import com.data.ntt.customer_service.interfaces.dto.request.CustomerRequest;
import com.data.ntt.customer_service.interfaces.dto.response.CustomerResponse;

public final class CustomerDtoMapper {

    private CustomerDtoMapper() {
    }

    public static Customer toDomain(CustomerRequest request) {
        if (request == null) {
            return null;
        }

        Person person = Person.builder()
                .name(request.getName())
                .gender(request.getGender())
                .identification(request.getIdentification())
                .address(request.getAddress())
                .phone(request.getPhone())
                .build();

        return Customer.builder()
                .person(person)
                .passwordHash(request.getPassword())
                .status(Boolean.TRUE)
                .build();
    }

    public static Customer toPatchDomain(CustomerPatchRequest request) {
        if (request == null) {
            return null;
        }

        Person person = Person.builder()
                .name(request.getName())
                .address(request.getAddress())
                .phone(request.getPhone())
                .build();

        return Customer.builder()
                .person(person)
                .passwordHash(request.getPassword())
                .status(request.getStatus())
                .build();
    }

    public static CustomerResponse toResponse(Customer customer) {
        if (customer == null || customer.getPerson() == null) {
            return null;
        }

        Person person = customer.getPerson();

        return CustomerResponse.builder()
                .name(person.getName())
                .gender(person.getGender())
                .identification(person.getIdentification())
                .address(person.getAddress())
                .phone(person.getPhone())
                .status(customer.getStatus())
                .build();
    }
}
