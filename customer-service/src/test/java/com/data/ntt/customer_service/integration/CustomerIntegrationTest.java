package com.data.ntt.customer_service.integration;

import com.data.ntt.customer_service.application.service.CustomerService;
import com.data.ntt.customer_service.domain.enums.Gender;
import com.data.ntt.customer_service.domain.model.Customer;
import com.data.ntt.customer_service.domain.model.Person;
import java.util.concurrent.ThreadLocalRandom;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("test")
class CustomerIntegrationTest {

    @Autowired
    private CustomerService customerService;

    @Test
    void shouldCreateAndLoadCustomerByIdentification() {
        String identification = nextIdentification();
        Customer created = createCustomer(identification);
        assertNotNull(created);
        assertNotNull(created.getPerson());
        assertEquals(identification, created.getPerson().getIdentification());

        Customer loaded = customerService.getCustomerByIdentification(identification).block();
        assertNotNull(loaded);
        assertNotNull(loaded.getPerson());
        assertEquals(identification, loaded.getPerson().getIdentification());
    }

    @Test
    void shouldDeactivateCustomerOnDelete() {
        String identification = nextIdentification();
        createCustomer(identification);

        customerService.deleteCustomerByIdentification(identification).block();

        Customer loaded = customerService.getCustomerByIdentification(identification).block();
        assertNotNull(loaded);
        assertEquals(Boolean.FALSE, loaded.getStatus());
    }

    private Customer createCustomer(String identification) {
        Person person = Person.builder()
                .name("Test User")
                .gender(Gender.MALE)
                .identification(identification)
                .address("Test Address 123")
                .phone("0987654321")
                .build();

        Customer customer = Customer.builder()
                .person(person)
                .passwordHash("secret")
                .build();

        return customerService.createCustomer(customer).block();
    }

    private String nextIdentification() {
        long value = ThreadLocalRandom.current().nextLong(1_000_000_000L, 9_999_999_999L);
        return String.format("%010d", value);
    }
}
