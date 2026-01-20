# Smart E-commerce System

A small, educational JavaFX-based e-commerce sample application demonstrating a layered architecture (DAO → Store → Service → Controller), simple caching, and JDBC-based data access with HikariCP for connection pooling.

This repository is intended for learning and experimentation — GUI screens for admin and buyer flows are implemented using JavaFX FXML, and the backend uses plain JDBC against PostgreSQL.

## Prerequisites
- Java 21 (or the JDK configured in `pom.xml`)
- PostgreSQL database accessible from your machine
- (Optional) A desktop environment to run JavaFX applications

## Environment configuration
The application expects database connection configuration to be available at runtime. Use the provided `.env.example` in the project root as a guide to create your own `.env` file (or set these as environment variables):

```
DB_HOST=localhost
DB_PORT=1234
DB_NAME=example_db
DB_USER=postgres
DB_PASSWORD=examplePassword

```

## Project SQL
A SQL file that creates the necessary PostgreSQL schema is provided at `db/schema.sql`. Use that file to create the database and tables required by the application.

How to run (development)

Build and run via the JavaFX Maven plugin (recommended for development):

```powershell
mvn javafx:run
```

This will compile the sources and launch the JavaFX application. The plugin is configured in `pom.xml` with the application's main class `org.example.EcommerceApplication`.

## Running tests

Execute the unit tests with Maven:

```powershell
mvn test
```

The repository contains unit tests that use JUnit 5 and Mockito.

## Performance and profiling

Performance data and profiling outputs (query plans, screenshots and timing charts) are documented in `PERFORMANCE.md`. Refer to that file for the benchmarking results and images.

Architecture notes

The codebase follows a layered approach that separates responsibilities:
- DAO (JdbcDao implementations) — low-level SQL and mapping, methods accept a `Connection` so stores can manage transactions.
- Store — composes DAO calls, handles explicit transactions when multiple DAOs must be updated together, and interacts with the cache before/after DB operations.
- Service — application-level business logic, depends on stores.
- Controller — JavaFX controllers call services and update UI.

This separation makes it easy to swap DAO implementations (e.g., for tests), manage transactions centrally in stores, and keep UI controllers focused on presentation.

## Branching strategy

We follow a simple, predictable branch naming convention to make reviews and automation easy:
- `feature/<short-description>` — work that adds a new feature or user-facing change.
- `refactor/<short-description>` — internal changes that alter structure without changing external behavior.
- `chore/<short-description>` — tooling, configuration, minor maintenance (e.g., dependency bumps, ci config).

Examples:
- `feature/product-reviews`
- `refactor/dao-store-service`
- `chore/update-javafx-plugin`

Guidelines
- Open a new branch per unit of work and open a pull request targeted at `develop` (or the team's default integration branch).
- Keep PRs small and focused; include a short description, testing notes, and screenshots where helpful.

---