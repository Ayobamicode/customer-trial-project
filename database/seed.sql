-- Fictional sample data. Seed only an empty table; do not duplicate rows on restart.
INSERT INTO CUSTOMER (name, address_line1, address_line2, city, state, zip)
SELECT * FROM (VALUES
 ('Alex Morgan', '1200 Example Avenue', 'Suite 200', 'Houston', 'TX', '77002'),
 ('Jordan Rivera', '450 Sample Street', '', 'Austin', 'TX', '78701'),
 ('Taylor Chen', '88 Demo Lane', 'Apt 3B', 'Dallas', 'TX', '75201')
) AS samples(name, address_line1, address_line2, city, state, zip)
WHERE NOT EXISTS (SELECT 1 FROM CUSTOMER);
