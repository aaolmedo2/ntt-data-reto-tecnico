package com.data.ntt.customer_service.infrastructure.persistence.repository;

import com.data.ntt.customer_service.infrastructure.persistence.entity.CustomerEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CustomerRepository extends JpaRepository<CustomerEntity, Long> {
    @Query("select c from CustomerEntity c join fetch c.person p where p.identification = :identification")
    Optional<CustomerEntity> findByPersonIdentification(@Param("identification") String identification);
}
