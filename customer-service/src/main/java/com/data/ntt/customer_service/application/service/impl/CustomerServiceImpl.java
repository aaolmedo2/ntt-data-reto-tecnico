package com.data.ntt.customer_service.application.service.impl;

import com.data.ntt.customer_service.application.service.CustomerService;
import com.data.ntt.customer_service.domain.model.Customer;
import com.data.ntt.customer_service.domain.model.Person;
import com.data.ntt.customer_service.infrastructure.persistence.entity.CustomerEntity;
import com.data.ntt.customer_service.infrastructure.persistence.entity.PersonEntity;
import com.data.ntt.customer_service.infrastructure.persistence.mapper.CustomerEntityMapper;
import com.data.ntt.customer_service.infrastructure.persistence.mapper.PersonEntityMapper;
import com.data.ntt.customer_service.infrastructure.persistence.repository.CustomerRepository;
import com.data.ntt.customer_service.infrastructure.persistence.repository.PersonRepository;
import com.data.ntt.customer_service.shared.exception.BadRequestException;
import com.data.ntt.customer_service.shared.exception.ConflictException;
import com.data.ntt.customer_service.shared.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final PersonRepository personRepository;
    private final CustomerRepository customerRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final TransactionTemplate transactionTemplate;

    @Override
    public Mono<Customer> createCustomer(Customer customer) {
        return Mono.fromCallable(() -> transactionTemplate.execute(status -> createCustomerBlocking(customer)))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<Customer> getCustomerByIdentification(String identification) {
        return Mono.fromCallable(() -> transactionTemplate.execute(
                status -> getCustomerByIdentificationBlocking(identification)))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<Customer> updateCustomerByIdentification(String identification, Customer patch) {
        return Mono.fromCallable(() -> transactionTemplate.execute(
                status -> updateCustomerByIdentificationBlocking(identification, patch)))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<Void> deleteCustomerByIdentification(String identification) {
        return Mono.fromRunnable(() -> transactionTemplate.executeWithoutResult(
                status -> deleteCustomerByIdentificationBlocking(identification)))
                .subscribeOn(Schedulers.boundedElastic())
                .then();
    }

    protected Customer createCustomerBlocking(Customer customer) {
        validateCreateRequest(customer);

        String identification = customer.getPerson().getIdentification();
        if (personRepository.existsByIdentification(identification)) {
            throw new ConflictException(String.format("Identification '%s' is already registered", identification));
        }

        PersonEntity personEntity = PersonEntityMapper.toEntity(customer.getPerson());
        CustomerEntity customerEntity = CustomerEntity.builder()
                .person(personEntity)
                .password(passwordEncoder.encode(customer.getPasswordHash()))
                .status(Boolean.TRUE)
                .build();
        personEntity.setCustomer(customerEntity);

        PersonEntity savedPerson = personRepository.save(personEntity);
        CustomerEntity savedCustomer = savedPerson.getCustomer();
        if (savedCustomer == null) {
            savedCustomer = customerRepository.findById(savedPerson.getId())
                    .orElseThrow(() -> new NotFoundException("Customer could not be loaded after creation"));
        }

        log.info("Customer created identification={}", identification);
        return CustomerEntityMapper.toDomain(savedCustomer);
    }

    protected Customer getCustomerByIdentificationBlocking(String identification) {
        validateIdentification(identification);

        CustomerEntity customerEntity = customerRepository.findByPersonIdentification(identification)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Customer with identification '%s' not found", identification)));

        return CustomerEntityMapper.toDomain(customerEntity);
    }

    protected Customer updateCustomerByIdentificationBlocking(String identification, Customer patch) {
        validateIdentification(identification);
        validatePatchRequest(patch);

        CustomerEntity customerEntity = customerRepository.findByPersonIdentification(identification)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Customer with identification '%s' not found", identification)));

        PersonEntity personEntity = customerEntity.getPerson();
        Person patchPerson = patch.getPerson();
        if (patchPerson != null) {
            if (patchPerson.getName() != null) {
                personEntity.setName(patchPerson.getName());
            }
            if (patchPerson.getAddress() != null) {
                personEntity.setAddress(patchPerson.getAddress());
            }
            if (patchPerson.getPhone() != null) {
                personEntity.setPhone(patchPerson.getPhone());
            }
        }

        if (patch.getPasswordHash() != null) {
            customerEntity.setPassword(passwordEncoder.encode(patch.getPasswordHash()));
        }

        if (patch.getStatus() != null) {
            customerEntity.setStatus(patch.getStatus());
        }

        CustomerEntity savedCustomer = customerRepository.save(customerEntity);
        log.info("Customer updated identification={}", identification);
        return CustomerEntityMapper.toDomain(savedCustomer);
    }

    protected void deleteCustomerByIdentificationBlocking(String identification) {
        validateIdentification(identification);

        CustomerEntity customerEntity = customerRepository.findByPersonIdentification(identification)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Customer with identification '%s' not found", identification)));

        customerEntity.setStatus(Boolean.FALSE);
        customerRepository.save(customerEntity);
        log.info("Customer deactivated identification={}", identification);
    }

    private void validateCreateRequest(Customer customer) {
        if (customer == null || customer.getPerson() == null) {
            throw new BadRequestException("Customer payload is required");
        }
        validateIdentification(customer.getPerson().getIdentification());
        if (customer.getPasswordHash() == null || customer.getPasswordHash().isBlank()) {
            throw new BadRequestException("Password is required");
        }
    }

    private void validatePatchRequest(Customer patch) {
        if (patch == null) {
            throw new BadRequestException("Customer patch payload is required");
        }

        boolean hasPersonFields = false;
        if (patch.getPerson() != null) {
            hasPersonFields = patch.getPerson().getName() != null
                    || patch.getPerson().getAddress() != null
                    || patch.getPerson().getPhone() != null;
        }

        boolean hasPassword = patch.getPasswordHash() != null;
        boolean hasStatus = patch.getStatus() != null;

        if (!hasPersonFields && !hasPassword && !hasStatus) {
            throw new BadRequestException("No fields provided for update");
        }
    }

    private void validateIdentification(String identification) {
        if (identification == null || identification.isBlank()) {
            throw new BadRequestException("Identification must not be blank");
        }
    }
}
