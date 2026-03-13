# GridOps — Utility Incident and Asset Management Platform

A full-stack enterprise operations platform for managing utility infrastructure assets, tracking incidents through a defined lifecycle, and integrating with legacy SOAP-based telemetry systems. Built as a modular monolith with Java 17, Spring Boot 3.2, PostgreSQL 16, and Angular 17.

## Why This Project Exists

GridOps was designed as a portfolio project targeting **Application Developer** roles in the utility industry. It demonstrates proficiency across the full enterprise stack — not as a toy CRUD app, but as a realistic internal operations tool with domain-appropriate complexity:

- Role-based access control with JWT authentication
- Asset lifecycle management with inspection tracking
- Incident/ticket management with an enforced state machine and audit trail
- Legacy system integration via SOAP/XML with a clean REST adapter
- A professional Angular SPA frontend with enterprise-style UI

The architecture favors clean layered design, maintainability, and interview-defensibility over technical novelty.

## Key Features

- **JWT Authentication & RBAC** — Stateless auth with Admin, Engineer, and Operator roles enforced at the endpoint level
- **Asset Management** — CRUD for utility assets (substations, transformers, line segments, switches, meters) with status tracking, inspections, and pagination
- **Incident Lifecycle** — Create, assign, transition, and resolve incidents with a state machine (`OPEN → ASSIGNED → IN_PROGRESS → RESOLVED → CLOSED`) and full audit history
- **SOAP Telemetry Integration** — Contract-first XSD with JAXB code generation, a standalone SOAP mock simulating a legacy SCADA system, and a clean adapter service that converts SOAP/XML to REST/JSON
- **Dashboard** — Aggregated metrics across assets and incidents
- **Angular Frontend** — Login, dashboard, asset list/detail with live telemetry, incident list/create/detail with inline status transitions and assignment actions, audit timeline
- **63 Backend Tests** — Unit tests (Mockito) and controller integration tests (`@WebMvcTest`) across all modules

## Architecture

GridOps uses a **modular monolith** pattern — a single Spring Boot application composed of four domain modules sharing one JVM and one PostgreSQL database. This provides clear domain separation without the operational overhead of microservices.

```
┌───────────────────────────────────────────────────────┐
│                    gridops-app                         │
│         (entry point, config, security, Flyway)        │
│                                                        │
│  ┌──────────┐  ┌──────────┐  ┌─────────────┐          │
│  │  auth    │  │  asset   │  │  incident   │          │
│  │  module  │  │  module  │  │  module     │          │
│  └──────────┘  └────┬─────┘  └─────────────┘          │
│                     │                                  │
│               ┌─────┴───────┐                          │
│               │ integration │                          │
│               │   module    │                          │
│               └──────┬──────┘                          │
└──────────────────────┼────────────────────────────────┘
                       │ SOAP/XML
              ┌────────┴────────┐
              │ gridops-soap-   │
              │ mock (port 8081)│
              └─────────────────┘

┌───────────────────────────────────────────────────────┐
│              gridops-ui (port 4200)                    │
│                  Angular 17 SPA                        │
│         REST/JSON ←→ gridops-app (port 8080)           │
└───────────────────────────────────────────────────────┘
```

**Why modular monolith, not microservices:** For a solo-developed portfolio project, microservices add operational complexity (service discovery, distributed transactions, container orchestration) without meaningful benefit. The modular monolith demonstrates the same domain separation and clean boundaries while remaining practical to run and review.

## Tech Stack

| Layer | Technologies |
|---|---|
| **Backend** | Java 17, Spring Boot 3.2, Spring Security 6, Spring Data JPA, Hibernate 6 |
| **Database** | PostgreSQL 16, Flyway migrations |
| **SOAP Integration** | Spring-WS, JAXB, contract-first XSD |
| **API Documentation** | OpenAPI 3 / Swagger UI (springdoc) |
| **Frontend** | Angular 17 (standalone components), TypeScript, CSS |
| **Testing** | JUnit 5, Mockito, Spring `@WebMvcTest` |
| **Infrastructure** | Docker Compose, Maven multi-module |

## Backend Module Breakdown

### `gridops-auth`
User entity, JWT token provider, authentication filter, `UserDetailsService`. Exposes `UserService` and `UserSummaryDto` as the cross-module contract — other modules resolve usernames by ID through this service, never by direct entity access.

### `gridops-asset`
Asset and AssetInspection entities with full CRUD, pagination, and filtering. Inspection creation is restricted to engineers. Username resolution for `createdBy` and `inspectedBy` uses batch caching to avoid N+1 queries. Also orchestrates telemetry retrieval through the integration module.

### `gridops-incident`
Incident entity with a state machine encoded in the `IncidentStatus` enum. Status transitions are validated before persistence. Every field change (status, severity, assignment, description) is recorded in the `incident_history` table as an immutable audit row. Incident numbers are generated atomically using a PostgreSQL `SEQUENCE`.

### `gridops-integration`
SOAP client adapter using Spring-WS and JAXB. Calls the external SOAP mock, maps the XML response to a clean `TelemetryDto`, and exposes `TelemetryAdapterService` as the public API. Failures are caught and wrapped in `TelemetryUnavailableException` (mapped to HTTP 503), so the rest of the system degrades gracefully.

### `gridops-app`
Application entry point, `SecurityConfig`, `GlobalExceptionHandler`, `DashboardController`, and all Flyway migrations. Ties the modules together without owning business logic.

### `gridops-soap-mock` (standalone)
A separate Spring Boot application simulating a legacy SCADA telemetry provider. Exposes a WSDL endpoint at `http://localhost:8081/ws/telemetry.wsdl` and generates deterministic telemetry data (temperature, load, voltage, power output) based on asset tag hashing.

## Frontend Pages

| Page | Route | Description |
|---|---|---|
| Login | `/login` | Username/password form, JWT stored in localStorage |
| Dashboard | `/dashboard` | Summary cards — total assets, assets offline, open incidents, critical incidents |
| Asset List | `/assets` | Paginated table with type/status filters, URL-driven state |
| Asset Detail | `/assets/:id` | Asset metadata, live SOAP telemetry display, inspection history |
| Incident List | `/incidents` | Paginated table with status filter, "New Incident" action |
| Create Incident | `/incidents/new` | Form with title, description, severity, optional asset link |
| Incident Detail | `/incidents/:id` | Full detail view, inline status transitions, admin assignment panel, audit timeline |

The frontend uses functional route guards, a functional HTTP interceptor for JWT injection, and Angular 17 control flow syntax (`@if`, `@for`). No component library — all UI is custom CSS for a clean enterprise aesthetic.

## SOAP Telemetry Integration

This is the most interview-relevant integration pattern in the project:

1. **Contract-first XSD** (`telemetry.xsd`) defines `GetTelemetryRequest` and `GetTelemetryResponse`
2. **JAXB code generation** (`jaxb2-maven-plugin`) produces Java classes from the XSD on both the mock server and the client
3. **`gridops-soap-mock`** implements a Spring-WS `@Endpoint` that returns telemetry data for any asset tag
4. **`gridops-integration`** uses `WebServiceTemplate` to call the SOAP endpoint, then maps the JAXB response to a clean `TelemetryDto`
5. **`AssetService`** calls `TelemetryAdapterService` — the controller never touches SOAP types directly
6. **Graceful degradation** — if the SOAP mock is down, the frontend shows "Telemetry unavailable" without breaking the page

## Project Structure

```
gridops-platform/
├── docker-compose.yml              # PostgreSQL 16
├── README.md
├── gridops-server/                  # Maven parent (pom packaging)
│   ├── gridops-app/                 # Entry point, config, Flyway, dashboard
│   │   └── src/main/resources/
│   │       └── db/migration/        # V1–V5 schema + V999 seed data
│   ├── gridops-auth/                # JWT, users, roles
│   ├── gridops-asset/               # Assets, inspections
│   ├── gridops-incident/            # Incidents, state machine, audit
│   └── gridops-integration/         # SOAP adapter client
├── gridops-soap-mock/               # Standalone SOAP telemetry server
└── gridops-ui/                      # Angular 17 SPA
    └── src/app/
        ├── core/                    # Services, guards, interceptors, models
        ├── shared/                  # Reusable components, pipes
        ├── features/                # auth, dashboard, assets, incidents
        └── layout/                  # App shell, sidebar, navbar
```

## Local Setup

### Prerequisites

- Java 17+ (JDK)
- Maven 3.9+
- Node.js 18+ and npm
- Docker and Docker Compose

### 1. Start PostgreSQL

```bash
docker-compose up -d
```

### 2. Build and run the backend

```bash
cd gridops-server
mvn clean install
cd gridops-app
mvn spring-boot:run
```

The API starts at `http://localhost:8080`. Swagger UI is available at `http://localhost:8080/swagger-ui.html`.

### 3. Start the SOAP telemetry mock

In a separate terminal:

```bash
cd gridops-soap-mock
mvn spring-boot:run
```

The SOAP service starts at `http://localhost:8081/ws`. Without this running, the telemetry section on asset detail pages will show "Telemetry unavailable" — this is intentional graceful degradation.

### 4. Start the Angular frontend

In a separate terminal:

```bash
cd gridops-ui
npm install
npx ng serve
```

The UI starts at `http://localhost:4200`.

## Demo Credentials

All seed users share the password: `password123`

| Username | Role | Description |
|---|---|---|
| `kgarcia` | ADMIN | Operations supervisor — assigns incidents, manages workflows |
| `jsmith` | ENGINEER | Field engineer — inspects assets, resolves incidents |
| `mjones` | ENGINEER | Field engineer — inspects assets, resolves incidents |
| `rthompson` | OPERATOR | Control room operator — monitors systems, reports incidents |

## Tests

63 backend tests across all modules:

| Module | Tests | Type |
|---|---|---|
| `gridops-asset` | 32 | Service unit tests (Mockito) + controller integration tests (`@WebMvcTest`) |
| `gridops-incident` | 19 | Controller integration tests covering CRUD, assignment, state transitions, authorization |
| `gridops-integration` | 4 | Mapper unit tests + adapter service tests with failure handling |
| `gridops-app` | 4 | Dashboard controller tests + application context load |

```bash
cd gridops-server
mvn clean test
```

## Future Improvements

These are realistic enhancements that were intentionally deferred from the MVP:

- **User administration** — Register, deactivate, and manage users through the UI
- **Asset creation/editing** — Frontend forms for asset CRUD (backend already supports it)
- **Incident search** — Full-text search across incident titles and descriptions
- **Email notifications** — Notify assigned engineers when incidents are created or escalated
- **CI/CD pipeline** — GitHub Actions for build, test, and Docker image publishing
- **Full-stack Docker Compose** — Single `docker-compose up` to run all three services
- **Production security** — Externalized JWT secret, HTTPS, refresh tokens, rate limiting

## License

This is a portfolio project for demonstration purposes. Not intended for production use.
