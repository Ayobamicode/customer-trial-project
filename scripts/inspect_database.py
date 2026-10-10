"""Print the actual PostgreSQL schema and rows for verification."""
from backend.db import connect
with connect() as conn:
    version = conn.execute('SHOW server_version').fetchone()['server_version']
    print('POSTGRESQL DATA LAYER — LIVE QUERY RESULTS')
    print('Server version:', version)
    print('Database: customerdb | Table: public.customer\n')
    print('CUSTOMER TABLE STRUCTURE')
    for row in conn.execute("SELECT column_name, data_type, character_maximum_length, is_nullable FROM information_schema.columns WHERE table_schema='public' AND table_name='customer' ORDER BY ordinal_position"):
        size = f"({row['character_maximum_length']})" if row['character_maximum_length'] else ''
        print(f"{row['column_name']:16} {row['data_type']+size:25} nullable: {row['is_nullable']}")
    print('\nSELECT * FROM customer ORDER BY customer_id;\n')
    for row in conn.execute('SELECT * FROM customer ORDER BY customer_id'):
        print(f"#{row['customer_id']}  {row['name']}")
        print('    '+row['address_line1'] + (', '+row['address_line2'] if row['address_line2'] else ''))
        print(f"    {row['city']}, {row['state']} {row['zip']}\n")
