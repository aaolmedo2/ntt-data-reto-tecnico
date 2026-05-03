package com.data.ntt.customer_service.interfaces.controller;

import com.data.ntt.customer_service.application.service.CustomerService;
import com.data.ntt.customer_service.interfaces.dto.mapper.CustomerDtoMapper;
import com.data.ntt.customer_service.interfaces.dto.request.CustomerPatchRequest;
import com.data.ntt.customer_service.interfaces.dto.request.CustomerRequest;
import com.data.ntt.customer_service.interfaces.dto.response.CustomerResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
@Validated
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping
    public Mono<ResponseEntity<CustomerResponse>> createCustomer(
            @Valid @RequestBody Mono<CustomerRequest> requestMono) {
        return requestMono
                .doOnNext(request -> log.info("Create customer identification={}", request.getIdentification()))
                .map(CustomerDtoMapper::toDomain)
                .flatMap(customerService::createCustomer)
                .map(CustomerDtoMapper::toResponse)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response));
    }

    @GetMapping("/{identification}")
    public Mono<ResponseEntity<CustomerResponse>> getCustomerByIdentification(
            @PathVariable String identification) {
        log.info("Get customer identification={}", identification);
        return customerService.getCustomerByIdentification(identification)
                .map(CustomerDtoMapper::toResponse)
                .map(ResponseEntity::ok);
    }

    @PatchMapping("/{identification}")
    public Mono<ResponseEntity<CustomerResponse>> updateCustomerByIdentification(
            @PathVariable String identification,
            @Valid @RequestBody Mono<CustomerPatchRequest> requestMono) {
        return requestMono
                .doOnNext(request -> log.info("Update customer identification={}", identification))
                .map(CustomerDtoMapper::toPatchDomain)
                .flatMap(patch -> customerService.updateCustomerByIdentification(identification, patch))
                .map(CustomerDtoMapper::toResponse)
                .map(ResponseEntity::ok);
    }

    @DeleteMapping("/{identification}")
    public Mono<ResponseEntity<Void>> deleteCustomerByIdentification(@PathVariable String identification) {
        log.info("Delete customer identification={}", identification);
        return customerService.deleteCustomerByIdentification(identification)
                .thenReturn(ResponseEntity.noContent().build());
    }
}
