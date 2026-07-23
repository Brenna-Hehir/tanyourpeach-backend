# Tan Your Peach Backend

Spring Boot backend for the Tan Your Peach application.

## Local prerequisites

- Docker Desktop
- Java 21
- Git

The Maven wrapper is included, so a separate Maven installation is not required.

## First-time local setup

Run all commands from the backend repository root.

### 1. Create your local environment file

```bash
cp .env.example .env
```

Open `.env` and replace `MYSQL_ROOT_PASSWORD` with a local password. The committed `.env.example` values are for local development only.

To generate a new JWT key, run:

```bash
openssl rand -base64 32
```

Paste the result into `JWT_SECRET` in `.env`.

The `.env` file is ignored by Git and must not be committed.

### 2. Start MySQL

```bash
docker compose up -d
```

This starts one MySQL 8.4 container named `tanyourpeach-db` and creates:

- `tanyourpeach` for local development
- `tanyourpeach_test` for backend integration tests

Check its status:

```bash
docker compose ps
```

Follow the initialization logs when needed:

```bash
docker compose logs -f mysql
```

Press `Control+C` to stop following the logs. The container continues running.

### 3. Start the backend

```bash
./mvnw spring-boot:run
```

The backend connects to MySQL on port `33306`. Flyway automatically runs the migrations in `src/main/resources/db/migration` and creates the application tables.

The local API runs at:

```text
http://localhost:8080
```

A basic public check is:

```bash
curl http://localhost:8080/api/services
```

A new database should return an empty JSON list until services are added.

## Verify the database and Flyway

Open MySQL with the application user:

```bash
docker exec -it tanyourpeach-db mysql -u springboot -p
```

Enter the `DB_PASSWORD` value from `.env`, then run:

```sql
USE tanyourpeach;
SHOW TABLES;
SELECT installed_rank, version, description, success
FROM flyway_schema_history
ORDER BY installed_rank;
```

Exit MySQL:

```sql
EXIT;
```

## Run backend tests

Keep the MySQL container running, then run:

```bash
./mvnw test
```

The integration tests use `tanyourpeach_test`. Flyway manages that schema separately from the local development schema.

## Normal daily startup

```bash
docker compose up -d
./mvnw spring-boot:run
```

## Stop local services

Stop MySQL without deleting its data:

```bash
docker compose stop
```

Start it again later:

```bash
docker compose start
```

Stop and remove the container while retaining the named volume:

```bash
docker compose down
```

## Completely reset the local databases

Warning: this permanently deletes all local Tan Your Peach database data.

```bash
docker compose down -v
```

The next `docker compose up -d` creates a fresh volume, reruns the MySQL initialization script, and allows Flyway to rebuild both schemas.

## Frontend

After the backend is running, open a separate terminal in the frontend repository:

```bash
npm install
npm run dev
```

The Vite frontend normally runs at `http://localhost:5173` and should use:

```text
VITE_API_BASE_URL=http://localhost:8080
```

## Production database

The production website should use a separate hosted MySQL database and production secrets. The local Docker setup is only for development and testing; it does not create or manage the future production database.