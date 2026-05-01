package com.data.ntt.customer_service.infrastructure.persistence.repository;

import com.data.ntt.customer_service.infrastructure.persistence.entity.PersonEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PersonRepository extends JpaRepository<PersonEntity, Long> {
    boolean existsByIdentification(String identification);

    Optional<PersonEntity> findByIdentification(String identification);
}
