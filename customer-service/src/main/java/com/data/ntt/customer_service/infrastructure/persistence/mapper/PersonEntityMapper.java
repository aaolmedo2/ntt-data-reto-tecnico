package com.data.ntt.customer_service.infrastructure.persistence.mapper;

import com.data.ntt.customer_service.domain.model.Person;
import com.data.ntt.customer_service.infrastructure.persistence.entity.PersonEntity;

public final class PersonEntityMapper {

    private PersonEntityMapper() {
    }

    public static Person toDomain(PersonEntity entity) {
        if (entity == null) {
            return null;
        }

        return Person.builder()
                .id(entity.getId())
                .name(entity.getName())
                .gender(entity.getGender())
                .identification(entity.getIdentification())
                .address(entity.getAddress())
                .phone(entity.getPhone())
                .build();
    }

    public static PersonEntity toEntity(Person person) {
        if (person == null) {
            return null;
        }

        return PersonEntity.builder()
                .id(person.getId())
                .name(person.getName())
                .gender(person.getGender())
                .identification(person.getIdentification())
                .address(person.getAddress())
                .phone(person.getPhone())
                .build();
    }
}
