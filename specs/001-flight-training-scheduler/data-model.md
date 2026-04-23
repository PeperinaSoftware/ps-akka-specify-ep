# Data Model: Flight Training Scheduler

**Feature**: `001-flight-training-scheduler`
**Phase**: 1 — Design

---

## Domain Objects (READ-ONLY — do not modify)

### `Participant` (domain)
```
id: String
participantType: ParticipantType { STUDENT, INSTRUCTOR, AIRCRAFT }
```

### `Timeslot` (domain — state of BookingSlotEntity)
```
bookings: Set<Timeslot.Booking>
available: Set<Participant>
```
Methods: `reserve`, `unreserve`, `book`, `cancelBooking`, `isWaiting`, `isBookable`, `findBooking`

### `BookingEvent` (domain — events of BookingSlotEntity)
```
ParticipantMarkedAvailable(slotId, participantId, participantType)  @TypeName("slot-reserved")
ParticipantUnmarkedAvailable(slotId, participantId, participantType) @TypeName("slot-unreserved")
ParticipantBooked(slotId, participantId, participantType, bookingId)  @TypeName("reservation-booked")
ParticipantCanceled(slotId, participantId, participantType, bookingId) @TypeName("booking-participant-canceled")
```

---

## Application Layer

### `BookingSlotEntity`
- **Key**: `slotId` (format: `YYYY-MM-DD-HH`)
- **State**: `Timeslot` (domain)
- **Events**: `BookingEvent` sealed interface (domain)
- **Commands**:

| Command | Effect | Events Emitted |
|---------|--------|----------------|
| `MarkSlotAvailable(Participant)` | persist | 1x `ParticipantMarkedAvailable` |
| `UnmarkSlotAvailable(Participant)` | persist | 1x `ParticipantUnmarkedAvailable` |
| `BookReservation(studentId, aircraftId, instructorId, bookingId)` | persist | 3x `ParticipantBooked` |
| `cancelBooking(String bookingId)` | persist | 3x `ParticipantCanceled` |
| `getSlot()` | reply | — |

- **Validation in `bookSlot`**: `currentState().isBookable(studentId, aircraftId, instructorId)` — error if false.
- **Validation in `cancelBooking`**: `currentState().findBooking(bookingId).isEmpty()` — error if true.

### `ParticipantSlotEntity`
- **Key**: `{slotId}-{participantId}` (composite, managed by consumer)
- **State**:
```
State(slotId: String, participantId: String, participantType: ParticipantType, status: String)
```
Status values: `"available"`, `"booked"`, `"canceled"`

- **Events**:
```
MarkedAvailable(slotId, participantId, participantType)   @TypeName("marked-available")
UnmarkedAvailable(slotId, participantId, participantType) @TypeName("unmarked-available")
Booked(slotId, participantId, participantType, bookingId) @TypeName("participant-booked")
Canceled(slotId, participantId, participantType, bookingId) @TypeName("participant-canceled")
```

- **emptyState()**: returns `null`
- **applyEvent()**: switch on event type → return new `State` with appropriate status

### `ParticipantSlotsView`
- **Source**: `ParticipantSlotEntity` events (via `@Consume.FromEventSourcedEntity`)
- **Row**:
```
SlotRow(slotId: String, participantId: String, participantType: String, bookingId: String, status: String)
```
- **Query parameter**: `ParticipantStatusInput(participantId: String, status: String)`
- **Result**: `SlotList(slots: List<SlotRow>)`
- **SQL**: `SELECT * AS slots FROM participant_slots_view WHERE participantId = :participantId AND status = :status`
- **Event handlers**:
  - `MarkedAvailable` → `updateRow(new SlotRow(..., null, "available"))`
  - `UnmarkedAvailable` → `deleteRow()` — row is removed entirely from the view
  - `Booked` → `updateRow(new SlotRow(..., bookingId, "booked"))`
  - `Canceled` → `updateRow(new SlotRow(..., bookingId, "canceled"))`

### `FlightConditionsAgent`
- **Session**: fresh UUID per booking request
- **Input**: `String timeSlotId`
- **Output**: `ConditionsReport(timeSlotId: String, meetsRequirements: Boolean)`
- **Tool**: `getWeatherForecast(String timeSlotId)` — returns mock weather; bad on day 13
- **Model**: `googleai-gemini` / `gemini-2.0-flash` (via `application.conf`)

---

## Event Flow

```
POST /flight/availability/{slotId}
  → BookingSlotEntity.markSlotAvailable
    → ParticipantMarkedAvailable
      → SlotToParticipantConsumer
        → ParticipantSlotEntity.markAvailable
          → MarkedAvailable
            → ParticipantSlotsView (status = "available")

POST /flight/bookings/{slotId}
  → FlightEndpoint validates future slot
  → FlightConditionsAgent.query(slotId) → ConditionsReport
  → [reject if !meetsRequirements]
  → BookingSlotEntity.bookSlot
    → 3x ParticipantBooked (student, aircraft, instructor)
      → SlotToParticipantConsumer (3 invocations)
        → ParticipantSlotEntity.book (3 times)
          → 3x Booked
            → ParticipantSlotsView (status = "booked")

DELETE /flight/bookings/{slotId}/{bookingId}
  → BookingSlotEntity.cancelBooking
    → 3x ParticipantCanceled
      → SlotToParticipantConsumer (3 invocations)
        → ParticipantSlotEntity.cancel (3 times)
          → 3x Canceled
            → ParticipantSlotsView (status = "canceled")
```

---

## State Transitions

### BookingSlotEntity (Timeslot)
```
[empty] → markAvailable → participant in available set
available → unmarkAvailable → removed from available set
available → bookSlot (all 3 present) → participant moved to bookings set
booked → cancelBooking → removed from bookings (not re-added to available)
```

### ParticipantSlotEntity (State.status)
```
null (emptyState) → markAvailable → "available"
"available" → unmarkAvailable → "available" (entry removed/ignored by view)
"available" → book → "booked"
"booked" → cancel → "canceled"
```
