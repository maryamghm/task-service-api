# Task Service API

Server-side API for a simple to-do app built with Java and Spring Boot. 
Tasks include title, description, status, priority, and creation date. 
Access is limited to authenticated users, with two demo users created at startup: UserA and UserB (passwords equal to usernames).

## Prerequisites
- Java 23 (or compatible)
- Docker (for local Postgres and Testcontainers)

## Run locally
1) Start Postgres with the provided schema:

```bash
docker compose up -d
```

2) Export a JWT secret:

```bash
export SECRET="your-secret-value"
```

3) Run the service:

```bash
./mvnw spring-boot:run
```

The API runs on `http://localhost:8080` by default.

## Run tests
Tests use Testcontainers and the schema in `src/main/resources/init.sql`.

```bash
./mvnw test
```

## API endpoints
- `POST /api/auth/login` -> get JWT token
- `POST /api/task` -> create task
- `GET /api/task` -> list tasks (supports `page`, `size`, `sortProperty`, `sortDir`)
- `GET /api/task/{id}` -> get a task
- `PUT /api/task/{id}` -> update a task
- `DELETE /api/task/{id}` -> delete a task
