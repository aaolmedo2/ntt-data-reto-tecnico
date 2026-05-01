package com.data.ntt.customer_service.application.service;

import com.data.ntt.customer_service.application.service.impl.CustomerServiceImpl;
import com.data.ntt.customer_service.domain.model.Customer;
import com.data.ntt.customer_service.domain.model.Person;
import com.data.ntt.customer_service.infrastructure.persistence.repository.CustomerRepository;
import com.data.ntt.customer_service.infrastructure.persistence.repository.PersonRepository;
import com.data.ntt.customer_service.shared.exception.BadRequestException;
import com.data.ntt.customer_service.shared.exception.ConflictException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.support.TransactionTemplate;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
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

        @Test
        void createCustomerShouldFailWhenIdentificationExists() {
                Person person = Person.builder()
                                .identification("1234567890")
                                .build();
                Customer customer = Customer.builder()
                                .person(person)
                                .passwordHash("secret")
                                .build();

                when(transactionTemplate.execute(any())).thenAnswer(invocation -> invocation
                                .getArgument(0, org.springframework.transaction.support.TransactionCallback.class)
                                .doInTransaction(null));
                when(personRepository.existsByIdentification("1234567890")).thenReturn(true);

                StepVerifier.create(customerService.createCustomer(customer))
                                .expectError(ConflictException.class)
                                .verify();
        }

        @Test
        void updateCustomerShouldFailWhenNoFieldsProvided() {
                Customer patch = Customer.builder().build();

                when(transactionTemplate.execute(any())).thenAnswer(invocation -> invocation
                                .getArgument(0, org.springframework.transaction.support.TransactionCallback.class)
                                .doInTransaction(null));

                StepVerifier.create(customerService.updateCustomerByIdentification("1234567890", patch))
                                .expectError(BadRequestException.class)
                                .verify();
        }
}
