package edu.hcc.customer;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api")
public class CustomerController {
    private final CustomerRepository customers;
    private final JdbcClient jdbc;

    public CustomerController(CustomerRepository customers, JdbcClient jdbc) {
        this.customers = customers;
        this.jdbc = jdbc;
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        String version = jdbc.sql("SHOW server_version").query(String.class).single();
        return Map.of("status", "ok", "framework", "Spring Boot",
                "database", "PostgreSQL", "version", version);
    }

    @GetMapping("/customers")
    public List<Customer> list() { return customers.findAll(); }

    @GetMapping("/customers/{id}")
    public Customer get(@PathVariable int id) {
        return customers.findById(id).orElseThrow(this::notFound);
    }

    @PostMapping("/customers")
    public ResponseEntity<Customer> create(@Valid @RequestBody CustomerInput input) {
        Customer customer = customers.create(input);
        return ResponseEntity.created(URI.create("/api/customers/" + customer.customerId()))
                .body(customer);
    }

    @PutMapping("/customers/{id}")
    public Customer update(@PathVariable int id, @Valid @RequestBody CustomerInput input) {
        return customers.update(id, input).orElseThrow(this::notFound);
    }

    @DeleteMapping("/customers/{id}")
    public ResponseEntity<Void> delete(@PathVariable int id) {
        if (!customers.delete(id)) throw notFound();
        return ResponseEntity.noContent().build();
    }

    private ResponseStatusException notFound() {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found");
    }
}
