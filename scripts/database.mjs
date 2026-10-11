import EmbeddedPostgres from "embedded-postgres";
import { existsSync, mkdirSync, readFileSync, writeFileSync } from "node:fs";
import { randomBytes } from "node:crypto";
import { resolve } from "node:path";
const dir = resolve(".runtime");
mkdirSync(dir, { recursive: true });
const configPath = resolve(dir, "database.json");
const config = existsSync(configPath)
  ? JSON.parse(readFileSync(configPath, "utf8"))
  : { password: randomBytes(24).toString("hex") };
writeFileSync(configPath, JSON.stringify(config), { mode: 0o600 });
const pg = new EmbeddedPostgres({
  databaseDir: resolve(dir, "postgres"),
  user: "customer_app",
  password: config.password,
  port: 55432,
  persistent: true,
  authMethod: "scram-sha-256",
  postgresFlags: ["-h", "127.0.0.1", "-k", ""],
});
if (!existsSync(resolve(dir, "postgres/PG_VERSION"))) await pg.initialise();
await pg.start();
const admin = pg.getPgClient();
await admin.connect();
const found = await admin.query(
  "SELECT 1 FROM pg_database WHERE datname = 'customerdb'",
);
if (!found.rowCount) await pg.createDatabase("customerdb");
await admin.end();
const database = pg.getPgClient('customerdb', '127.0.0.1');
await database.connect();
await database.query(readFileSync('database/schema.sql', 'utf8'));
await database.query(readFileSync('database/seed.sql', 'utf8'));
await database.end();
writeFileSync(resolve(dir, 'application-local.properties'),
  `spring.datasource.password=${config.password}\n`, { mode: 0o600 });
const url = `postgresql://customer_app:${config.password}@127.0.0.1:55432/customerdb`;
writeFileSync(resolve(dir, "database-url"), url, { mode: 0o600 });
console.log(
  "PostgreSQL ready on 127.0.0.1:55432 (customerdb). Schema and sample data initialized. Start Spring Boot next.",
);
let stopping = false;
async function stop() {
  if (stopping) return;
  stopping = true;
  await pg.stop();
  process.exit(0);
}
process.on("SIGINT", stop);
process.on("SIGTERM", stop);
setInterval(() => {}, 60000);
