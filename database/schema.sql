-- PostgreSQL folds the unquoted name CUSTOMER to customer.
CREATE TABLE IF NOT EXISTS CUSTOMER (
    customer_id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(100) NOT NULL CHECK (length(trim(name)) > 0),
    address_line1 VARCHAR(150) NOT NULL CHECK (length(trim(address_line1)) > 0),
    address_line2 VARCHAR(150) NOT NULL DEFAULT '',
    city VARCHAR(80) NOT NULL CHECK (length(trim(city)) > 0),
    state VARCHAR(2) NOT NULL CHECK (state ~ '^[A-Z]{2}$'),
    zip VARCHAR(10) NOT NULL CHECK (zip ~ '^[0-9]{5}(-[0-9]{4})?$')
);
