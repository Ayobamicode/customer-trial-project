package edu.hcc.customer;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.jdbc.core.simple.JdbcClient;
import static org.assertj.core.api.Assertions.assertThat;

/** End-to-end HTTP tests against the configured REAL PostgreSQL database. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CustomerApiTest {
    @Autowired TestRestTemplate http;
    @Autowired JdbcClient jdbc;

    Map<String, String> input(String name, String state, String zip) {
        return Map.of("name", name, "address_line1", "100 Test Street",
                "address_line2", "", "city", "Houston", "state", state, "zip", zip);
    }

    @Test void completeCrudPersistsToPostgreSQL() {
        var created = http.postForEntity("/api/customers", input("Integration Test", "tx", "07702"), Customer.class);
        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Customer c = created.getBody();
        int id = c.customerId();
        String url = "/api/customers/" + id;
        try {
            assertThat(created.getHeaders().getLocation().toString()).isEqualTo(url);
            assertThat(c.state()).isEqualTo("TX");
            assertThat(c.zip()).isEqualTo("07702");
            assertThat(http.getForObject(url, Customer.class).name()).isEqualTo("Integration Test");
            assertThat(http.getForObject("/api/customers", Customer[].class)).extracting(Customer::customerId).contains(id);
            var update = http.exchange(url, HttpMethod.PUT,
                    new HttpEntity<>(input("Updated Test", "TX", "77002-1234")), Customer.class);
            assertThat(update.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(jdbc.sql("SELECT name FROM customer WHERE customer_id=:id").param("id", id)
                    .query(String.class).single()).isEqualTo("Updated Test");
        } finally {
            assertThat(http.exchange(url, HttpMethod.DELETE, HttpEntity.EMPTY, Void.class).getStatusCode())
                    .isEqualTo(HttpStatus.NO_CONTENT);
        }
        assertThat(http.getForEntity(url, String.class).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(http.exchange(url, HttpMethod.PUT, new HttpEntity<>(input("Gone", "TX", "77002")), String.class)
                .getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(http.exchange(url, HttpMethod.DELETE, HttpEntity.EMPTY, String.class).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test void rejectsInvalidAndBlankFields() {
        var invalid = java.util.List.of(input("  ", "TX", "77002"), input("Test", "Texas", "77002"),
                input("Test", "TX", "bad"), input("Test", "TX", "123456"));
        for (var body : invalid) {
            var result = http.postForEntity("/api/customers", body, String.class);
            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(result.getBody()).contains("detail");
        }
    }

    @Test void healthUsesRealDatabase() {
        var result = http.getForEntity("/api/health", String.class);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).contains("Spring Boot", "PostgreSQL", "ok");
    }
}
