import EmbeddedPostgres from 'embedded-postgres';
import { readFileSync, writeFileSync } from 'node:fs';
const { password } = JSON.parse(readFileSync('.runtime/database.json', 'utf8'));
const pg = new EmbeddedPostgres({ user: 'customer_app', password, port: 55432 });
const db = pg.getPgClient('customerdb', '127.0.0.1');
await db.connect();
try {
  const {rows:[version]} = await db.query('SHOW server_version');
  const {rows:columns} = await db.query("SELECT column_name, data_type, character_maximum_length, is_nullable FROM information_schema.columns WHERE table_schema='public' AND table_name='customer' ORDER BY ordinal_position");
  const {rows} = await db.query('SELECT * FROM customer ORDER BY customer_id');
  const lines = ['POSTGRESQL DATA LAYER — LIVE QUERY RESULTS', 'Server version: '+version.server_version, 'Database: customerdb | Table: public.customer', '', 'CUSTOMER TABLE STRUCTURE'];
  for(const c of columns) lines.push(c.column_name.padEnd(17)+(c.data_type+(c.character_maximum_length?'('+c.character_maximum_length+')':'')).padEnd(26)+'nullable: '+c.is_nullable);
  lines.push('', 'SELECT * FROM customer ORDER BY customer_id;', '');
  for(const c of rows) lines.push('#'+c.customer_id+'  '+c.name,'    '+c.address_line1+(c.address_line2?', '+c.address_line2:''),'    '+c.city+', '+c.state+' '+c.zip,'');
  writeFileSync('database/verification.txt', lines.join('\n')+'\n');
  console.log(lines.join('\n'));
} finally { await db.end(); }
