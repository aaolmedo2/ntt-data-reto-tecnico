package com.data.ntt.customer_service.application.service;

import com.data.ntt.customer_service.application.service.impl.CustomerServiceImpl;
import com.data.ntt.customer_service.domain.enums.Gender;
import com.data.ntt.customer_service.domain.model.Customer;
import com.data.ntt.customer_service.domain.model.Person;
import com.data.ntt.customer_service.infrastructure.persistence.entity.CustomerEntity;
import com.data.ntt.customer_service.infrastructure.persistence.entity.PersonEntity;
import com.data.ntt.customer_service.infrastructure.persistence.repository.CustomerRepository;
import com.data.ntt.customer_service.infrastructure.persistence.repository.PersonRepository;
import com.data.ntt.customer_service.shared.exception.ConflictException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.support.TransactionTemplate;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerServiceImplTest {

        @Mock
        private PersonRepository personRepository;

        @Mock
        private CustomerRepository customerRepository;

        @Mock
        private BCryptPasswordEncoder passwordEncoder;

        @Mock
        private TransactionTemplate transactionTemplate;

        @InjectMocks
        private CustomerServiceImpl customerService;

        @BeforeEach
        void setUp() {
                when(transactionTemplate.execute(any())).thenAnswer(invocation -> invocation
                                .getArgument(0, org.springframework.transaction.support.TransactionCallback.class)
                                .doInTransaction(null));
        }

        @Test
        void createCustomerShouldCreateSuccessfully() {
                Person person = Person.builder()
                                .name("Test User")
                                .gender(Gender.MALE)
                                .identification("1234567890")
                                .address("Quito")
                                .phone("0987654321")
                                .build();
                Customer customer = Customer.builder()
                                .person(person)
                                .passwordHash("secret")
                                .build();

                PersonEntity savedPerson = PersonEntity.builder()
                                .id(1L)
                                .name(person.getName())
                                .gender(person.getGender())
                                .identification(person.getIdentification())
                                .address(person.getAddress())
                                .phone(person.getPhone())
                                .build();
                CustomerEntity savedCustomer = CustomerEntity.builder()
                                .id(1L)
                                .person(savedPerson)
                                .password("hashed")
                                .status(Boolean.TRUE)
                                .build();
                savedPerson.setCustomer(savedCustomer);

                when(personRepository.existsByIdentification("1234567890")).thenReturn(false);
                when(passwordEncoder.encode("secret")).thenReturn("hashed");
                when(personRepository.save(any(PersonEntity.class))).thenReturn(savedPerson);

                StepVerifier.create(customerService.createCustomer(customer))
                                .assertNext(result -> {
                                        org.junit.jupiter.api.Assertions.assertNotNull(result);
                                        org.junit.jupiter.api.Assertions.assertNotNull(result.getPerson());
                                        org.junit.jupiter.api.Assertions.assertEquals("1234567890",
                                                        result.getPerson().getIdentification());
                                        org.junit.jupiter.api.Assertions.assertEquals(Boolean.TRUE, result.getStatus());
                                        org.junit.jupiter.api.Assertions.assertEquals("hashed",
                                                        result.getPasswordHash());
                                })
                                .verifyComplete();
        }

        @Test
        void createCustomerShouldRejectDuplicateIdentification() {
                Person person = Person.builder()
                                .identification("1234567890")
                                .build();
                Customer customer = Customer.builder()
                                .person(person)
                                .passwordHash("secret")
                                .build();
                when(personRepository.existsByIdentification("1234567890")).thenReturn(true);

                StepVerifier.create(customerService.createCustomer(customer))
                                .expectError(ConflictException.class)
                                .verify();
        }

        @Test
        void updateCustomerShouldApplyPartialPatch() {
                PersonEntity existingPerson = PersonEntity.builder()
                                .id(1L)
                                .name("Old Name")
                                .gender(Gender.MALE)
                                .identification("1234567890")
                                .address("Manta")
                                .phone("0112233445")
                                .build();
                CustomerEntity existingCustomer = CustomerEntity.builder()
                                .id(1L)
                                .person(existingPerson)
                                .password("old")
                                .status(Boolean.TRUE)
                                .build();

                Customer patch = Customer.builder()
                                .person(Person.builder()
                                                .name("New Name")
                                                .address("New Address")
                                                .build())
                                .build();

                when(customerRepository.findByPersonIdentification("1234567890"))
                                .thenReturn(Optional.of(existingCustomer));
                when(customerRepository.save(any(CustomerEntity.class)))
                                .thenAnswer(invocation -> invocation.getArgument(0));

                StepVerifier.create(customerService.updateCustomerByIdentification("1234567890", patch))
                                .assertNext(result -> {
                                        org.junit.jupiter.api.Assertions.assertNotNull(result.getPerson());
                                        org.junit.jupiter.api.Assertions.assertEquals("New Name",
                                                        result.getPerson().getName());
                                        org.junit.jupiter.api.Assertions.assertEquals("New Address",
                                                        result.getPerson().getAddress());
                                        org.junit.jupiter.api.Assertions.assertEquals("0112233445",
                                                        result.getPerson().getPhone());
                                })
                                .verifyComplete();

                ArgumentCaptor<CustomerEntity> captor = ArgumentCaptor.forClass(CustomerEntity.class);
                verify(customerRepository).save(captor.capture());
                org.junit.jupiter.api.Assertions.assertEquals("New Name", captor.getValue().getPerson().getName());
                org.junit.jupiter.api.Assertions.assertEquals("New Address",
                                captor.getValue().getPerson().getAddress());
                org.junit.jupiter.api.Assertions.assertEquals("0112233445", captor.getValue().getPerson().getPhone());
        }
}
