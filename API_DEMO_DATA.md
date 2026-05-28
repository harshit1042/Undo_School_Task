# Recording Script: API Demo (Swagger Click-by-Click)

This file is optimized for your 5-10 min recording.

- Swagger URL: `http://localhost:8080/swagger-ui.html`
- Base URL: `http://localhost:8080`
- Run app first: `mvn spring-boot:run`

---

## 0) One-time Setup for Fresh Demo

If you want clean data before recording, run this in MySQL:

```sql
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE bookings;
TRUNCATE TABLE sessions;
TRUNCATE TABLE offerings;
TRUNCATE TABLE parents;
TRUNCATE TABLE courses;
SET FOREIGN_KEY_CHECKS = 1;
```

---

## Demo IDs / Timezones

Use these exact values everywhere:

- `TEACHER_ID`: `11111111-1111-1111-1111-111111111111`
- `PARENT_ID`: `22222222-2222-2222-2222-222222222222`
- `TEACHER_TZ`: `America/New_York`
- `PARENT_TZ`: `Asia/Kolkata`

---

## 1) Create Offering #1 (Teacher)

Creates a new class offering/batch under a course for a teacher.

### Swagger API
`POST /api/teacher/offerings`

### Request body
```json
{
  "courseTitle": "Minecraft Coding",
  "offeringName": "Saturday Batch",
  "teacherId": "11111111-1111-1111-1111-111111111111",
  "teacherTimezone": "America/New_York"
}
```

### After Execute
- Copy `offeringId` from response and store as: `OFFERING_1_ID`

---

## 2) Add Sessions to Offering #1 (Teacher)

Adds one or more class sessions (time slots) to an existing offering.

### Swagger API
`POST /api/teacher/offerings/{offeringId}/sessions`

### Path param
- `offeringId` = `OFFERING_1_ID`

### Request body
```json
[
  {
    "startAtLocal": "2026-06-06T18:00:00",
    "endAtLocal": "2026-06-06T19:00:00"
  },
  {
    "startAtLocal": "2026-06-13T18:00:00",
    "endAtLocal": "2026-06-13T19:00:00"
  },
  {
    "startAtLocal": "2026-06-20T18:00:00",
    "endAtLocal": "2026-06-20T19:00:00"
  }
]
```

### What to say in recording
- "Teacher created sessions in New York timezone."

---

## 3) View Teacher Offerings (Timezone Proof #1)

Returns all offerings of a teacher with sessions shown in requested timezone.

### Swagger API
`GET /api/teacher/{teacherId}/offerings`

### Params
- `teacherId` = `11111111-1111-1111-1111-111111111111`
- `timezone` = `America/New_York`

### Expected
- Session times shown around `18:00 -04:00` (NY local)

---

## 4) View Parent Offerings (Timezone Proof #2)

Returns all available offerings for parents with session times converted to their timezone.

### Swagger API
`GET /api/parent/offerings`

### Query param
- `timezone` = `Asia/Kolkata`

### Expected
- Same sessions shown converted to India time (e.g. around `03:30 +05:30` next day)

### What to say in recording
- "Same class times are converted to parent's local timezone."

---

## 5) Book Offering #1 (Parent)

Books the complete offering for a parent (all sessions included).

### Swagger API
`POST /api/parent/bookings`

### Query param
- `timezone` = `Asia/Kolkata`

### Request body
```json
{
  "parentId": "22222222-2222-2222-2222-222222222222",
  "offeringId": "OFFERING_1_ID"
}
```

### Expected
- `200 OK`
- Response has booking details with sessions in `Asia/Kolkata`

---

## 6) Get Parent Bookings

Fetches all offerings already booked by a parent.

### Swagger API
`GET /api/parent/{parentId}/bookings`

### Params
- `parentId` = `22222222-2222-2222-2222-222222222222`
- `timezone` = `Asia/Kolkata`

### Expected
- Shows booked offering and all sessions

---

## 7) Duplicate Booking Check (Must show 409)

Verifies system blocks re-booking the same offering for the same parent.

### Swagger API
`POST /api/parent/bookings`

### Query param
- `timezone` = `Asia/Kolkata`

### Request body (same as step 5)
```json
{
  "parentId": "22222222-2222-2222-2222-222222222222",
  "offeringId": "OFFERING_1_ID"
}
```

### Expected
- `409 Conflict`
- Message like: `Offering already booked by this parent`

---

## 8) Create Offering #2 for Conflict Test

Creates another offering that will be used to test overlap conflict.

### Swagger API
`POST /api/teacher/offerings`

### Request body
```json
{
  "courseTitle": "Roblox Game Design",
  "offeringName": "Overlap Batch",
  "teacherId": "11111111-1111-1111-1111-111111111111",
  "teacherTimezone": "UTC"
}
```

### After Execute
- Copy `offeringId` from response and store as: `OFFERING_2_ID`

---

## 9) Add Overlapping Session to Offering #2

Adds a session that intentionally overlaps with an already booked session window.

This session intentionally overlaps with already-booked Offering #1 session (`2026-06-13T22:00:00Z` to `23:00:00Z`).

### Swagger API
`POST /api/teacher/offerings/{offeringId}/sessions`

### Path param
- `offeringId` = `OFFERING_2_ID`

### Request body
```json
[
  {
    "startAtLocal": "2026-06-13T22:30:00",
    "endAtLocal": "2026-06-13T23:30:00"
  }
]
```

---

## 10) Conflict Booking Check (Must show 409)

Verifies system blocks booking when any session time overlaps previous bookings.

### Swagger API
`POST /api/parent/bookings`

### Query param
- `timezone` = `Asia/Kolkata`

### Request body
```json
{
  "parentId": "22222222-2222-2222-2222-222222222222",
  "offeringId": "OFFERING_2_ID"
}
```

### Expected
- `409 Conflict`
- Message like: `Booking conflicts with an already booked offering session`

---

## 11) (Optional) Validation Error Demo

Verifies request validation and proper `400 Bad Request` handling.

### Swagger API
`POST /api/teacher/offerings`

### Request body
```json
{
  "courseTitle": "",
  "offeringName": "",
  "teacherId": null,
  "teacherTimezone": "INVALID/TZ"
}
```

### Expected
- `400 Bad Request`

---

## Fast Copy Block (replace IDs only)

Use this when recording quickly:

```text
OFFERING_1_ID = <from step 1>
OFFERING_2_ID = <from step 8>
```

---

## Recording Checklist (Say this clearly)

- Created offering and sessions as teacher
- Showed timezone conversion (teacher tz vs parent tz)
- Parent booked complete offering
- Duplicate booking rejected (`409`)
- Overlapping offering booking rejected (`409`)
- Parent booking list endpoint works

