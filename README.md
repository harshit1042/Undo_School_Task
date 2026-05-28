# Global Class Offering Booking System

Backend assignment implementation for Undo School using Java Spring Boot and MySQL.

## What this service does

This service supports two user roles:

- **Teacher** can create an offering (batch) for a course and add session schedules.
- **Parent/Student** can view offerings in their own timezone, book an offering, and view their bookings.

Booking is done at **offering level** (all sessions together), not per session.

---

## Tech stack

- Java 21
- Spring Boot 3.5
- Spring Web
- Spring Data JPA (Hibernate)
- Flyway
- MySQL
- Swagger / OpenAPI (`springdoc-openapi`)

---

## Project structure

- `controller` -> REST APIs
- `service` -> business logic (timezone conversion, conflict checks, booking flow)
- `repository` -> JPA repositories
- `entity` -> DB entities
- `dto` -> API request/response objects
- `exception` -> centralized error handling
- `src/main/resources/db/migration` -> Flyway SQL migrations

---

## Local setup

### 1) Prerequisites

- Java 21+
- Maven 3.9+
- MySQL running locally

### 2) Create database

```sql
CREATE DATABASE undo_booking;
```

### 3) Configure environment variables (optional)

If you skip these, defaults from `application.yml` are used.

- `DB_URL` (default: `jdbc:mysql://localhost:3306/undo_booking?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC`)
- `DB_USERNAME` (default: `root`)
- `DB_PASSWORD` (default: `root`)
- `PORT` (default: `8080`)

Example:

```bash
export DB_URL="jdbc:mysql://localhost:3306/undo_booking?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
export DB_USERNAME="root"
export DB_PASSWORD=""
```

### 4) Run application

```bash
mvn spring-boot:run
```

Flyway migration runs automatically on startup.

---

## API documentation

- **Without running the project (recommended for reviewers):**
  - `openapi.json` (repo root)
  - `openapi.yaml` (repo root)
- **When running locally:**
  - Swagger UI: `http://localhost:8080/swagger-ui.html`
  - OpenAPI JSON endpoint: `http://localhost:8080/v3/api-docs`

---

## APIs

### Teacher APIs

- `POST /api/teacher/offerings` -> create offering
- `POST /api/teacher/offerings/{offeringId}/sessions` -> add sessions to offering
- `GET /api/teacher/{teacherId}/offerings?timezone=UTC` -> list teacher offerings

### Parent APIs

- `GET /api/parent/offerings?timezone=UTC` -> list available offerings
- `POST /api/parent/bookings?timezone=UTC` -> book offering
- `GET /api/parent/{parentId}/bookings?timezone=UTC` -> list parent bookings

---

## Example payloads

### Create offering

```json
{
  "courseTitle": "Minecraft Coding",
  "offeringName": "Saturday Batch",
  "teacherId": "11111111-1111-1111-1111-111111111111",
  "teacherTimezone": "America/New_York"
}
```

### Add sessions

```json
[
  {
    "startAtLocal": "2026-06-06T18:00:00",
    "endAtLocal": "2026-06-06T19:00:00"
  },
  {
    "startAtLocal": "2026-06-13T18:00:00",
    "endAtLocal": "2026-06-13T19:00:00"
  }
]
```

### Book offering

```json
{
  "parentId": "22222222-2222-2222-2222-222222222222",
  "offeringId": "<offering-id>"
}
```

---

## Database tables

- `courses` -> course names/titles
- `offerings` -> course offerings with teacher and timezone
- `sessions` -> offering sessions stored in UTC
- `parents` -> parent identity mapping
- `bookings` -> parent to offering bookings

IDs are exposed as standard 36-character UUID strings in API contracts.

---

## Core implementation decisions

### Timezone handling

- Teacher sends local datetime + teacher timezone.
- Datetime is converted to UTC before saving.
- All read APIs convert UTC times into requested `timezone` query param.

### Conflict detection

A booking is rejected if **any session** of candidate offering overlaps with already booked sessions for the same parent.

Overlap condition:

`existing.start < candidate.end && existing.end > candidate.start`

### Concurrency handling

Booking flow is transactional and locks the parent row with pessimistic write lock before checking conflicts.

This prevents race conditions when multiple requests for the same parent happen at the same time.

Also, duplicate booking of same offering is blocked by DB unique constraint on `(parent_id, offering_id)`.

---

## Common errors

- `400 Bad Request` -> validation issue / invalid timezone / invalid session range
- `404 Not Found` -> offering not found
- `409 Conflict` -> duplicate booking or overlapping booking

---

## Demo script

Use `API_DEMO_DATA.md` for recording-ready, step-by-step request flow including:

- normal teacher + parent flow
- duplicate booking check (`409`)
- overlap conflict check (`409`)

---

## Notes / assumptions

- No seat-capacity constraint is implemented (not part of assignment requirements).
- Parent books full offering, not individual sessions.
- Session overlap inside same offering is not allowed.
