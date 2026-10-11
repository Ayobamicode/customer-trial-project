package edu.hcc.customer;

import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class CustomerRepository {
    private final JdbcClient jdbc;
    private final RowMapper<Customer> mapper = (rs, row) -> new Customer(
            rs.getInt("customer_id"), rs.getString("name"),
            rs.getString("address_line1"), rs.getString("address_line2"),
            rs.getString("city"), rs.getString("state"), rs.getString("zip"));

    public CustomerRepository(JdbcClient jdbc) { this.jdbc = jdbc; }

    public List<Customer> findAll() {
        return jdbc.sql("SELECT * FROM customer ORDER BY customer_id")
                .query(mapper).list();
    }

    public Optional<Customer> findById(int id) {
        return jdbc.sql("SELECT * FROM customer WHERE customer_id = :id")
                .param("id", id).query(mapper).optional();
    }

    public Customer create(CustomerInput c) {
        return fields(jdbc.sql("""
                INSERT INTO customer (name, address_line1, address_line2, city, state, zip)
                VALUES (:name, :line1, :line2, :city, :state, :zip) RETURNING *
                """), c).query(mapper).single();
    }

    public Optional<Customer> update(int id, CustomerInput c) {
        return fields(jdbc.sql("""
                UPDATE customer SET name=:name, address_line1=:line1,
                  address_line2=:line2, city=:city, state=:state, zip=:zip
                WHERE customer_id=:id RETURNING *
                """), c).param("id", id).query(mapper).optional();
    }

    public boolean delete(int id) {
        return jdbc.sql("DELETE FROM customer WHERE customer_id=:id")
                .param("id", id).update() == 1;
    }

    private JdbcClient.StatementSpec fields(JdbcClient.StatementSpec sql, CustomerInput c) {
        return sql.param("name", c.name()).param("line1", c.addressLine1())
                .param("line2", c.addressLine2()).param("city", c.city())
                .param("state", c.state()).param("zip", c.zip());
    }
}
