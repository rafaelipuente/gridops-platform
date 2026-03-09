# GridOps — Utility Incident and Asset Management Platform

An enterprise-style modular monolith for managing utility assets and incidents, built with Java 17, Spring Boot 3.2, PostgreSQL, and Angular.

## Architecture

GridOps is a **modular monolith** — a single Spring Boot application with four domain modules sharing one JVM and one PostgreSQL database:

| Module | Responsibility |
|---|---|
| `gridops-app` | Application entry point, configuration, Flyway migrations |
| `gridops-auth` | User identity, JWT authentication, role enforcement |
| `gridops-asset` | Utility asset lifecycle, inspections |
| `gridops-incident` | Incident/ticket lifecycle, state machine, audit trail |
| `gridops-integration` | SOAP-to-REST adapter for legacy telemetry |

A standalone `gridops-soap-mock` simulates a legacy SCADA telemetry service.

## Tech Stack

- **Backend:** Java 17, Spring Boot 3.2, Spring Security, Spring Data JPA, Hibernate
- **Database:** PostgreSQL 16, Flyway migrations
- **API Docs:** OpenAPI 3 / Swagger UI
- **Frontend:** Angular (planned)
- **Infrastructure:** Docker Compose

## Prerequisites

- Java 17+ (JDK)
- Maven 3.9+
- Docker and Docker Compose

## Quick Start

```bash
# 1. Start PostgreSQL
docker-compose up -d

# 2. Build the project
cd gridops-server
mvn clean install

# 3. Run the application
cd gridops-app
mvn spring-boot:run

# 4. Open Swagger UI
open http://localhost:8080/swagger-ui.html
```

## Seed Credentials

All seed users have password: `password123`

| Username | Role | Description |
|---|---|---|
| `kgarcia` | ADMIN | Operations supervisor — assigns incidents, closes resolved tickets |
| `jsmith` | ENGINEER | Field engineer — performs inspections, resolves incidents on site |
| `mjones` | ENGINEER | Field engineer — performs inspections, resolves incidents on site |
| `rthompson` | OPERATOR | Control room operator — monitors SCADA, reports incidents |

## Project Structure

```
gridops/
├── docker-compose.yml
├── gridops-server/           # Maven parent (packaging: pom)
│   ├── gridops-app/          # Runnable module — main class, config, Flyway
│   ├── gridops-auth/         # Auth module — users, JWT, roles
│   ├── gridops-asset/        # Asset module — assets, inspections
│   ├── gridops-incident/     # Incident module — tickets, state machine, audit
│   └── gridops-integration/  # Integration module — SOAP adapter
├── gridops-soap-mock/        # Standalone SOAP telemetry mock
└── gridops-ui/               # Angular frontend
```

## License

This is a portfolio project — not intended for production use.
