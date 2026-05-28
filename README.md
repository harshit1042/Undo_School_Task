# Global Class Offering Booking System

Backend assignment implementation for Undo School using Java Spring Boot and MySQL.

## What this service does

This service supports two user roles:

- **Teacher** creates offerings and adds class sessions.
- **Parent/Student** views offerings in local timezone, books an offering, and views bookings.

Booking is done at **offering level** (all sessions together), not per session.

---

## Tech stack

- Java 21
- Spring Boot 3.5
- Spring Web
- Spring Data JPA (Hibernate)
- Flyway
- MySQL
- Swagger UI

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

### 3) Configure env vars (optional)
Defaults are in `application.yml`.

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

### 4) Run app
```bash
mvn spring-boot:run
```

---

## API documentation

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI endpoint: `http://localhost:8080/v3/api-docs`

All endpoint details are documented below as well.

---

## Data model

- `courses` -> course metadata (e.g., Minecraft Coding)
- `offerings` -> schedulable batch/section under course
- `sessions` -> actual class times for each offering (stored in UTC)
- `parents` -> parent identity
- `bookings` -> parent booked offering relation

API IDs use standard UUID string format.

---

## API contract (complete)

## Teacher APIs

### 1) Create offering
**Endpoint**: `POST /api/teacher/offerings`

**What it does**: Creates a new offering under a course for a teacher.

**Request body**
```json
{
  "courseTitle": "Minecraft Coding",
  "offeringName": "Saturday Batch",
  "teacherId": "11111111-1111-1111-1111-111111111111",
  "teacherTimezone": "America/New_York"
}
```

**Success response (200)**
```json
{
  "offeringId": "8141d4c7-cec3-4f8f-baca-6de0a190779b",
  "courseTitle": "Minecraft Coding",
  "offeringName": "Saturday Batch",
  "teacherId": "11111111-1111-1111-1111-111111111111",
  "teacherTimezone": "America/New_York",
  "sessions": []
}
```

---

### 2) Add sessions to offering
**Endpoint**: `POST /api/teacher/offerings/{offeringId}/sessions`

**What it does**: Adds one or more sessions to an existing offering.

**Path params**
- `offeringId` (UUID)

**Request body**
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

**Success response (200)**
```json
{
  "offeringId": "8141d4c7-cec3-4f8f-baca-6de0a190779b",
  "courseTitle": "Minecraft Coding",
  "offeringName": "Saturday Batch",
  "teacherId": "11111111-1111-1111-1111-111111111111",
  "teacherTimezone": "America/New_York",
  "sessions": [
    {
      "sessionId": "9fe50fcd-b34e-44cc-8b9b-61c791489857",
      "startAt": "2026-06-06T18:00:00-04:00",
      "endAt": "2026-06-06T19:00:00-04:00"
    }
  ]
}
```

---

### 3) Get teacher offerings
**Endpoint**: `GET /api/teacher/{teacherId}/offerings?timezone=UTC`

**What it does**: Returns offerings of a teacher; session times are converted to requested timezone.

**Path params**
- `teacherId` (UUID)

**Query params**
- `timezone` (optional, default `UTC`)

**Success response (200)**
```json
[
  {
    "offeringId": "8141d4c7-cec3-4f8f-baca-6de0a190779b",
    "courseTitle": "Minecraft Coding",
    "offeringName": "Saturday Batch",
    "teacherId": "11111111-1111-1111-1111-111111111111",
    "teacherTimezone": "America/New_York",
    "sessions": [
      {
        "sessionId": "9fe50fcd-b34e-44cc-8b9b-61c791489857",
        "startAt": "2026-06-06T22:00:00Z",
        "endAt": "2026-06-06T23:00:00Z"
      }
    ]
  }
]
```

---

## Parent APIs

### 4) Get available offerings
**Endpoint**: `GET /api/parent/offerings?timezone=UTC`

**What it does**: Returns all available offerings with sessions converted to requested timezone.

**Query params**
- `timezone` (optional, default `UTC`)

**Success response (200)**
```json
[
  {
    "offeringId": "8141d4c7-cec3-4f8f-baca-6de0a190779b",
    "courseTitle": "Minecraft Coding",
    "offeringName": "Saturday Batch",
    "teacherId": "11111111-1111-1111-1111-111111111111",
    "teacherTimezone": "America/New_York",
    "sessions": [
      {
        "sessionId": "9fe50fcd-b34e-44cc-8b9b-61c791489857",
        "startAt": "2026-06-06T22:00:00Z",
        "endAt": "2026-06-06T23:00:00Z"
      }
    ]
  }
]
```

---

### 5) Book offering
**Endpoint**: `POST /api/parent/bookings?timezone=Asia/Kolkata`

**What it does**: Books complete offering for parent (all sessions).

**Query params**
- `timezone` (optional, default `UTC`)

**Request body**
```json
{
  "parentId": "22222222-2222-2222-2222-222222222222",
  "offeringId": "8141d4c7-cec3-4f8f-baca-6de0a190779b"
}
```

**Success response (200)**
```json
{
  "bookingId": "15eb3d20-b616-4c28-ad58-6a047155ef9e",
  "parentId": "22222222-2222-2222-2222-222222222222",
  "offeringId": "8141d4c7-cec3-4f8f-baca-6de0a190779b",
  "courseTitle": "Minecraft Coding",
  "offeringName": "Saturday Batch",
  "bookedAt": "2026-05-28T16:26:34.862384+05:30",
  "sessions": [
    {
      "sessionId": "9fe50fcd-b34e-44cc-8b9b-61c791489857",
      "startAt": "2026-06-07T03:30:00+05:30",
      "endAt": "2026-06-07T04:30:00+05:30"
    }
  ]
}
```

---

### 6) Get parent bookings
**Endpoint**: `GET /api/parent/{parentId}/bookings?timezone=UTC`

**What it does**: Returns all offerings booked by a parent.

**Path params**
- `parentId` (UUID)

**Query params**
- `timezone` (optional, default `UTC`)

**Success response (200)**
```json
[
  {
    "bookingId": "15eb3d20-b616-4c28-ad58-6a047155ef9e",
    "parentId": "22222222-2222-2222-2222-222222222222",
    "offeringId": "8141d4c7-cec3-4f8f-baca-6de0a190779b",
    "courseTitle": "Minecraft Coding",
    "offeringName": "Saturday Batch",
    "bookedAt": "2026-05-28T10:56:34.862384Z",
    "sessions": [
      {
        "sessionId": "9fe50fcd-b34e-44cc-8b9b-61c791489857",
        "startAt": "2026-06-06T22:00:00Z",
        "endAt": "2026-06-06T23:00:00Z"
      }
    ]
  }
]
```

---

## Error responses

### Validation error (400)
```json
{
  "message": "courseTitle must not be blank",
  "timestamp": "2026-05-28T11:30:00Z"
}
```

### Not found (404)
```json
{
  "message": "Offering not found: <offering-id>",
  "timestamp": "2026-05-28T11:31:00Z"
}
```

### Duplicate booking (409)
```json
{
  "message": "Offering already booked by this parent",
  "timestamp": "2026-05-28T11:25:00Z"
}
```

### Overlap conflict (409)
```json
{
  "message": "Booking conflicts with an already booked offering session",
  "timestamp": "2026-05-28T11:20:00Z"
}
```

---

## Key implementation points

### Timezone handling
- Teacher sends session time in teacher timezone.
- Service converts and stores as UTC.
- Read APIs convert UTC to requested timezone.

### Conflict handling
- Parent cannot book offerings with overlapping session windows.
- Overlap logic: `existing.start < candidate.end && existing.end > candidate.start`

### Concurrency handling
- Booking transaction uses pessimistic lock on parent row.
- Prevents race conditions for parallel requests.
- DB unique constraint blocks same parent booking same offering twice.

---

## Demo flow

For quick recording with exact sequence, use: `API_DEMO_DATA.md`.
