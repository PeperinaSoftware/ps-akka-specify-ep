# API Contracts: Flight Training Scheduler

**Feature**: `001-flight-training-scheduler`
**Base path**: `/flight`

---

## Endpoints

### POST `/flight/availability/{slotId}`
Mark a participant as available for a time slot.

**Request body**:
```json
{ "participantId": "alice", "participantType": "student" }
```
`participantType` values: `student`, `instructor`, `aircraft` (case-insensitive)

**Responses**:
- `200 OK` — availability recorded
- `400 Bad Request` — invalid `participantType`

---

### DELETE `/flight/availability/{slotId}`
Remove a participant's availability mark.

**Request body**:
```json
{ "participantId": "alice", "participantType": "student" }
```

**Responses**:
- `200 OK` — availability removed
- `400 Bad Request` — invalid `participantType`

---

### GET `/flight/availability/{slotId}`
Retrieve the internal availability state for a slot.

**Response body** (`Timeslot`):
```json
{
  "bookings": [],
  "available": [
    { "id": "alice", "participantType": "STUDENT" },
    { "id": "superteacher", "participantType": "INSTRUCTOR" },
    { "id": "superplane", "participantType": "AIRCRAFT" }
  ]
}
```

---

### POST `/flight/bookings/{slotId}`
Book a training flight. Requires all three participants to be available. Flight conditions must be approved by the agent.

**Request body**:
```json
{
  "studentId": "alice",
  "aircraftId": "superplane",
  "instructorId": "superteacher",
  "bookingId": "booking4"
}
```

**Responses**:
- `201 Created` — booking confirmed
- `400 Bad Request` — slot is in the past, participants not available, or flight conditions not met

---

### DELETE `/flight/bookings/{slotId}/{bookingId}`
Cancel an existing booking.

**Responses**:
- `200 OK` — booking canceled
- `400 Bad Request` — booking not found

---

### GET `/flight/slots/{participantId}/{status}`
Retrieve time slots for a participant filtered by status.

`status` values: `available`, `booked`

**Response body** (`SlotList`):
```json
{
  "slots": [
    {
      "slotId": "2025-12-10-10",
      "participantId": "alice",
      "participantType": "STUDENT",
      "bookingId": "booking4",
      "status": "booked"
    }
  ]
}
```

---

## Notes

- `slotId` format: `YYYY-MM-DD-HH` (e.g., `2025-12-10-10` = December 10, 2025 at 10:00)
- Use day `13` in the slot date to trigger bad weather conditions for testing (e.g., `2025-12-13-10`)
- Bookings require all three participant types; the client must pass all IDs explicitly
