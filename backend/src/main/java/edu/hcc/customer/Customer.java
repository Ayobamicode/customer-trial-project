package edu.hcc.customer;

/** JSON uses snake_case to match the Angular model and PostgreSQL columns. */
public record Customer(
        int customerId, String name, String addressLine1,
        String addressLine2, String city, String state, String zip) {}
