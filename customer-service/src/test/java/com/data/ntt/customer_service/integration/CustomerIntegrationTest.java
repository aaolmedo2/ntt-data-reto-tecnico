package com.data.ntt.customer_service.integration;

import com.data.ntt.customer_service.application.service.CustomerService;
import com.data.ntt.customer_service.domain.model.Customer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class CustomerIntegrationTest {

    @Autowired
    private CustomerService customerService;

    @Test
    void shouldLoadExistingCustomerByIdentification() {
        StepVerifier.create(customerService.getCustomerByIdentification("1753898111"))
                .assertNext(customer -> {
                    assertNotNull(customer);
                    assertNotNull(customer.getPerson());
                    assertEquals("1753898111", customer.getPerson().getIdentification());
                })
                .verifyComplete();
    }
}
