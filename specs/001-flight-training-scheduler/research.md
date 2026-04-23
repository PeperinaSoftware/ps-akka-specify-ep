# Research: Flight Training Scheduler

**Feature**: `001-flight-training-scheduler`
**Phase**: 0 — Research

---

## 1. Persisting Multiple Events from a Single Command

**Decision**: Use `effects().persist(List.of(event1, event2, event3)).thenReply(...)` to emit exactly 3 events per booking/cancellation.

**Rationale**: `bookSlot` must emit one `ParticipantBooked` event per participant (student, aircraft, instructor). The Akka SDK supports `persist(List<Event>)` for atomic multi-event persistence from a single command handler. The same applies to `cancelBooking` emitting 3 `ParticipantCanceled` events.

**Alternatives considered**: Emitting a single aggregate event — rejected because the `SlotToParticipantConsumer` and domain design require individual events per participant, and the domain already defines per-participant event types.

---

## 2. ParticipantSlotEntity emptyState

**Decision**: Return `null` from `emptyState()`. This is the only method allowed to return null per Akka SDK rules.

**Rationale**: The `State` record has no concept of "not yet created". The entity will only be queried/commanded after at least one event has been applied, so `null` is the correct initial state marker. `applyEvent` must never return null — all 4 event types must produce a non-null `State`.

**Alternatives considered**: Adding an `isEmpty` flag to `State` — rejected (YAGNI, adds complexity, domain record should stay clean).

---

## 3. Future Slot Validation

**Decision**: Parse the `slotId` string (`YYYY-MM-DD-HH`) in the endpoint and compare against `LocalDateTime.now()`. Return `400 Bad Request` if the slot is in the past.

**Rationale**: The spec states "Bookings can only be created for future time slots." This is a boundary validation — it belongs in the HTTP endpoint, not the entity, since it depends on wall-clock time which would make entity tests fragile.

**Alternatives considered**: Validating in `BookingSlotEntity.bookSlot` — rejected; entities should not depend on wall-clock time for correctness, and the endpoint is the right boundary.

---

## 4. FlightConditionsAgent — VFR Criteria

**Decision**: Use the following standard VFR minimums in the SYSTEM_MESSAGE:
- Visibility ≥ 3 statute miles
- Ceiling ≥ 1000 feet AGL
- Wind speed ≤ 25 knots
- No active thunderstorms or severe weather

**Rationale**: These are standard FAA VFR minimums for Class G/E airspace. They are well-known, do not require an external call, and allow the agent to make a deterministic judgment.

**Alternatives considered**: Using MVFR/IFR distinctions — rejected (over-engineering for a mock scheduler).

---

## 5. FlightConditionsAgent — Mock Weather Tool

**Decision**: The `getWeatherForecast` tool returns bad conditions (low visibility, high winds) when the day portion of the slotId is `13` (e.g., `2025-12-13-10`). All other dates return good VFR conditions.

**Rationale**: This gives testers a deterministic way to get both good and bad outcomes without external API calls — just use day 13 for bad conditions. Easy to remember and document.

**Alternatives considered**: Random conditions — rejected (non-deterministic, untestable). Hour-based — rejected (less memorable than date-based).

---

## 6. responseAs vs responseConformsTo for Agent

**Decision**: Keep `responseAs(ConditionsReport.class)` as in the stub. Add explicit JSON schema instructions to the SYSTEM_MESSAGE so the model returns well-formed JSON.

**Rationale**: The stub already uses `responseAs`; switching to `responseConformsTo` would be a larger change. The key requirement is that the SYSTEM_MESSAGE instructs the model to return `{"timeSlotId": "...", "meetsRequirements": true/false}`.

**Alternatives considered**: `responseConformsTo` — preferred by AGENTS.md but would require stub refactoring beyond scope.

---

## 7. ParticipantSlotsView Query

**Decision**:
```sql
SELECT * AS slots FROM participant_slots_view WHERE participantId = :participantId AND status = :status
```
Wrapped in `SlotList(List<SlotRow> slots)` return type. The `bookingId` field is `null`/empty for `available` rows and populated for `booked` rows.

**Rationale**: The view's `SlotRow` has all the fields needed. `SlotList` already wraps `List<SlotRow>`. The `AS slots` alias matches the field name in `SlotList`.

---

## 8. SlotToParticipantConsumer — Routing Logic

**Decision**: Pattern-match on `BookingEvent` type; for each case extract fields and call the corresponding `ParticipantSlotEntity` command via `ComponentClient`.

**Rationale**: The consumer must handle all 4 event types: `ParticipantMarkedAvailable`, `ParticipantUnmarkedAvailable`, `ParticipantBooked`, `ParticipantCanceled`. Each maps to a single entity command. The `participantSlotId()` helper already provides the composite key.

**Note on bookSlot**: Since `bookSlot` emits 3 `ParticipantBooked` events (one per participant), the consumer will be invoked 3 times — once per event. No fan-out logic needed in the consumer itself.
