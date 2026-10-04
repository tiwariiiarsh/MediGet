# MediGet — Backend

REST API for **MediGet**, an app that helps people find medicines at nearby pharmacies and gives pharmacy owners (sellers) tools to manage their shop, stock, billing and sales analytics.

Frontend repo: [MediGetFrontend](https://github.com/tiwariiiarsh/MediGetFrontend) · Live API: https://mediget.onrender.com

## Tech stack

| Area | Tools |
|------|-------|
| Language | Java 17 |
| Framework | Spring Boot 3.2 (Web, Data JPA, Security, Validation) |
| Auth | JWT (jjwt) with role based access (`USER`, `SELLER`, `ADMIN`) |
| Database | PostgreSQL 16 |
| Docs | Swagger / OpenAPI (springdoc) |
| Build | Maven (wrapper included) |
| DevOps | Docker, Docker Compose, GitHub Actions, GHCR, Render |

## Features
- Sign up / sign in with JWT, role based authorization
- Public medicine search, paginated listing and **nearby search** by location and radius
- Alternative medicine suggestions
- Seller shop management (create, update, delete, upload image)
- Medicine CRUD per shop with image upload
- Billing with automatic sales count updates
- Analytics: revenue, top / least selling, low stock

## Project structure

```
src/main/java/com/example/MediSearch/
├── controller/   # REST endpoints
├── service/      # Business logic
├── repository/   # Spring Data JPA repositories
├── model/        # JPA entities (User, Shop, Medicine, Bill, ...)
├── payload/      # DTOs
├── security/     # JWT, Spring Security, CORS config
├── exceptions/   # Global exception handling
├── config/       # App config and constants
└── Utils/        # Auth and distance helpers
```

## Environment variables

Copy `.env.example` to `.env` and fill it in.

| Variable | Description |
|----------|-------------|
| `DB_URL` | JDBC URL, e.g. `jdbc:postgresql://localhost:5432/medisearch` |
| `DB_USERNAME` / `DB_PASSWORD` | Database credentials |
| `JWT_SECRET` | Secret used to sign JWTs (long random string) |
| `FRONTEND_URL` | Allowed CORS origin(s), comma separated, no trailing `/` |
| `IMAGE_BASE_URL` | Public base URL for uploaded images |
| `PORT` | Server port (default `8080`, set automatically on Render) |

## Running locally

### Option 1 — Docker Compose (recommended)

Starts PostgreSQL and the backend together.

```bash
cp .env.example .env      # then edit values
docker compose up -d --build
```

- API: http://localhost:8080
- Uploaded images are stored in `./images` (mounted volume)
- Postgres data persists in the `postgres-data` volume

Stop with `docker compose down`.

### Option 2 — Maven

Requires Java 17 and a running PostgreSQL.

```bash
export DB_URL=jdbc:postgresql://localhost:5432/medisearch DB_USERNAME=postgres DB_PASSWORD=... JWT_SECRET=...
./mvnw spring-boot:run
```

## Docker

The `Dockerfile` is a multi-stage build:
1. **Build stage** — `eclipse-temurin:17-jdk`, downloads dependencies (cached layer) and runs `mvnw package`.
2. **Run stage** — slim `eclipse-temurin:17-jre` image that only contains the final jar.

```bash
docker build -t mediget-backend .
docker run -p 8080:8080 --env-file .env mediget-backend
```

## CI/CD pipeline

Defined in `.github/workflows/ci-cd.yml`. Runs on every push / PR to `main`.

| Job | When | What it does |
|-----|------|--------------|
| `build-and-test` | Every push and PR | Starts a Postgres 16 service, sets up Java 17, runs `./mvnw clean verify` |
| `docker` | Push to `main` only | Builds the image and pushes it to GHCR as `ghcr.io/tiwariiiarsh/mediget-backend:latest` and `:<commit-sha>` |
| `deploy` | After `docker` | SSH into a server and runs `docker compose up -d --build`. Skipped automatically if `SSH_HOST` is not set |

**Required GitHub secrets**

| Secret | Used by |
|--------|---------|
| `CI_DB_PASSWORD` | Test database password |
| `CI_JWT_SECRET` | JWT secret for tests |
| `SSH_HOST`, `SSH_USER`, `SSH_KEY`, `APP_DIR` | Optional, only for SSH deploy |

## Deployment (Render)

The live API runs on Render as a Docker web service:
- Render builds from the `Dockerfile` on every push to `main`
- Set `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`, `FRONTEND_URL`, `IMAGE_BASE_URL` in the Render environment
- `FRONTEND_URL` must be `https://medigetfrontend.onrender.com`, otherwise the browser blocks requests with a CORS error

## API overview

Base path: `/api`. Swagger UI: `/swagger-ui/index.html`

**Auth** — `/api/auth`
| Method | Path | Description |
|--------|------|-------------|
| POST | `/signup` | Register |
| POST | `/signin` | Log in, returns JWT |
| POST | `/signout` | Log out |
| GET | `/user`, `/username` | Current user |

**Public**
| Method | Path | Description |
|--------|------|-------------|
| GET | `/public/medicines` | Paginated medicines |
| GET | `/public/medicines/nearby` | Search near a location (`keyword`, `userLat`, `userLng`, `radiusKm`) |
| GET | `/public/medicines/{medicineId}/alternatives` | Alternatives |
| GET | `/public/shop/{shopId}` | Shop details |

**Seller** (role `SELLER` or `ADMIN`)
| Method | Path | Description |
|--------|------|-------------|
| GET / POST / PUT / DELETE | `/seller/shop` | Manage your shop |
| PUT | `/seller/shop/{shopId}/image` | Upload shop image |
| GET | `/seller/shop/{shopId}/medicines` | List shop medicines |
| POST | `/seller/shop/{shopId}/medicine` | Add medicine |
| PUT / DELETE | `/seller/shop/{shopId}/medicine/{medicineId}` | Update / delete medicine |
| PUT | `/seller/shop/{shopId}/medicine/{medicineId}/image` | Upload medicine image |
| POST | `/seller/shop/{shopId}/bill` | Create a bill |
| GET | `/seller/shop/{shopId}/analytics/{revenue,top-selling,least-selling,best,least,low-stock}` | Analytics |
