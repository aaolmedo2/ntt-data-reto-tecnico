package com.data.ntt.customer_service.infrastructure.persistence.mapper;

import com.data.ntt.customer_service.domain.model.Customer;
import com.data.ntt.customer_service.infrastructure.persistence.entity.CustomerEntity;
import com.data.ntt.customer_service.infrastructure.persistence.entity.PersonEntity;

public final class CustomerEntityMapper {

    private CustomerEntityMapper() {
    }

    public static Customer toDomain(CustomerEntity entity) {
        if (entity == null) {
            return null;
        }

        return Customer.builder()
                .id(entity.getId())
                .person(PersonEntityMapper.toDomain(entity.getPerson()))
                .passwordHash(entity.getPassword())
                .status(entity.getStatus())
                .build();
    }

    public static CustomerEntity toEntity(Customer customer) {
        if (customer == null) {
            return null;
        }

        PersonEntity personEntity = PersonEntityMapper.toEntity(customer.getPerson());

        return CustomerEntity.builder()
                .id(personEntity != null ? personEntity.getId() : customer.getId())
                .person(personEntity)
                .password(customer.getPasswordHash())
                .status(customer.getStatus())
                .build();
    }
}
