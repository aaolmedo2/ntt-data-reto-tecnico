package com.data.ntt.customer_service.application.service;

import com.data.ntt.customer_service.domain.model.Customer;
import reactor.core.publisher.Mono;

public interface CustomerService {
    Mono<Customer> createCustomer(Customer customer);

    Mono<Customer> getCustomerByIdentification(String identification);

    Mono<Customer> updateCustomerByIdentification(String identification, Customer patch);

    Mono<Void> deleteCustomerByIdentification(String identification);
}
