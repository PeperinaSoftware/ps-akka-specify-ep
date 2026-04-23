# Tasks: Flight Training Scheduler

**Input**: Design documents from `/specs/001-flight-training-scheduler/`
**Prerequisites**: plan.md ✓, spec.md ✓, research.md ✓, data-model.md ✓, contracts/api.md ✓

**No setup phase needed** — scaffold already exists, compiles clean (`mvn compile` verified).

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies on incomplete tasks)
- **[Story]**: Which user story this task belongs to
- Tests are **not** requested — not included in phases below

---

## Phase 2: Foundational — Core Entities & Agent

**Purpose**: Implement the three independent components that have no cross-dependencies. All user stories block on this phase.

**⚠️ CRITICAL**: All three user story phases depend on this phase being complete.

- [x] T001 Implement `BookingSlotEntity` in `src/main/java/io/example/application/BookingSlotEntity.java`: fill `markSlotAvailable` (emit `ParticipantMarkedAvailable`), `unmarkSlotAvailable` (emit `ParticipantUnmarkedAvailable`), `bookSlot` (validate `isBookable`, emit 3x `ParticipantBooked` via `effects().persist(List.of(...))`), `cancelBooking` (validate `findBooking` not empty, emit 3x `ParticipantCanceled` for each booking entry), `getSlot` (return `effects().reply(currentState())`), and `applyEvent` (switch on event → delegate to `Timeslot.reserve`, `unreserve`, `book`, `cancelBooking`)

- [x] T002 [P] Implement `ParticipantSlotEntity` in `src/main/java/io/example/application/ParticipantSlotEntity.java`: add `emptyState()` returning `null`; fill `markAvailable` (persist `MarkedAvailable`, reply `Done.done()`), `unmarkAvailable` (persist `UnmarkedAvailable`, reply `Done.done()`), `book` (persist `Booked`, reply `Done.done()`), `cancel` (persist `Canceled`, reply `Done.done()`); fill `applyEvent` switch returning new `State` with status `"available"` for `MarkedAvailable`/`UnmarkedAvailable`, `"booked"` (with bookingId) for `Booked`, `"canceled"` (with bookingId) for `Canceled`

- [x] T003 [P] Implement `FlightConditionsAgent` in `src/main/java/io/example/application/FlightConditionsAgent.java`: write `SYSTEM_MESSAGE` with VFR criteria (visibility ≥ 3 sm, ceiling ≥ 1000 ft AGL, wind ≤ 25 kt, no thunderstorms), instruct the model to call `getWeatherForecast` tool and return JSON `{"timeSlotId":"...","meetsRequirements":true/false}`; implement `getWeatherForecast` to parse day from slotId (split by `-`, index 2) and return below-VFR conditions (visibility 1 mi, ceiling 400 ft, wind 35 kt) when day equals `"13"`, good VFR conditions (visibility 10 mi, ceiling 5000 ft, wind 8 kt) otherwise; update `query` to pass `timeSlotId` in `userMessage(timeSlotId)`

**Checkpoint**: Run `mvn compile` — must pass with zero errors before proceeding.

---

## Phase 3: User Story 1 — Availability Management (P1) 🎯 MVP

**Goal**: Participants can mark/unmark availability, and those changes propagate to the view.

**User Stories covered**: US1 (mark availability), US2 (unmark availability)

**Independent Test**: Mark 3 participants available in a slot → `GET /flight/availability/{slotId}` shows them in `available` list; `GET /flight/slots/{participantId}/available` returns the slot.

- [x] T004 [US1] Implement `ParticipantSlotsView.ParticipantSlotsViewUpdater.onEvent` in `src/main/java/io/example/application/ParticipantSlotsView.java`: switch on `ParticipantSlotEntity.Event` — `MarkedAvailable` → `effects().updateRow(new SlotRow(e.slotId(), e.participantId(), e.participantType().name(), null, "available"))`, `UnmarkedAvailable` → `effects().deleteRow()` (removes the row entirely — participant is no longer available), `Booked` → `effects().updateRow(new SlotRow(e.slotId(), e.participantId(), e.participantType().name(), e.bookingId(), "booked"))`, `Canceled` → `effects().updateRow(new SlotRow(e.slotId(), e.participantId(), e.participantType().name(), e.bookingId(), "canceled"))`; add `@Query("SELECT * AS slots FROM participant_slots_view WHERE participantId = :participantId AND status = :status")` annotation to `getSlotsByParticipantAndStatus`

- [x] T005 [P] [US1] Implement `SlotToParticipantConsumer.onEvent` in `src/main/java/io/example/application/SlotToParticipantConsumer.java`: switch on `BookingEvent` — `ParticipantMarkedAvailable` → call `componentClient.forEventSourcedEntity(ParticipantSlotEntity.class).call(participantSlotId(evt)).method(ParticipantSlotEntity::markAvailable).invoke(new Commands.MarkAvailable(evt.slotId(), evt.participantId(), evt.participantType()))`, `ParticipantUnmarkedAvailable` → call `unmarkAvailable`, `ParticipantBooked` → call `book` with `Commands.Book(...)` including `bookingId`, `ParticipantCanceled` → call `cancel` with `Commands.Cancel(...)` including `bookingId`; return `effects().done()` in each case

- [x] T006 [US1] Implement availability methods in `src/main/java/io/example/api/FlightEndpoint.java`: `markAvailable` → call `componentClient.forEventSourcedEntity(BookingSlotEntity.class).call(slotId).method(BookingSlotEntity::markSlotAvailable).invoke(new Command.MarkSlotAvailable(new Participant(request.participantId(), participantType)))` return `HttpResponses.ok()`; `unmarkAvailable` → same pattern with `unmarkSlotAvailable` and `Command.UnmarkSlotAvailable`; `getSlot` → call `componentClient.forEventSourcedEntity(BookingSlotEntity.class).call(slotId).method(BookingSlotEntity::getSlot).invoke()` and return result

**Checkpoint**: Run `mvn compile`. Mark 3 participants available and verify `GET /flight/availability/{slotId}` and `GET /flight/slots/{participantId}/available` return correct data.

---

## Phase 4: User Story 2 — Book a Training Flight (P2)

**Goal**: A student can book a slot when all 3 participants are available and flight conditions are approved.

**User Story covered**: US3 (book training flight with agent conditions check)

**Independent Test**: With 3 participants available on a future slot → `POST /flight/bookings/{slotId}` with valid IDs returns `201`; `GET /flight/slots/alice/booked` shows the slot. Using a day-13 slotId returns `400`.

- [x] T007 [US2] Implement `FlightEndpoint.createBooking` in `src/main/java/io/example/api/FlightEndpoint.java`: (1) parse slotId (`YYYY-MM-DD-HH`) into `LocalDateTime` using `DateTimeFormatter.ofPattern("yyyy-MM-dd-HH")` and reject with `HttpResponses.badRequest("Slot is in the past")` if not after `LocalDateTime.now()`; (2) call `componentClient.forAgent().inSession(UUID.randomUUID().toString()).method(FlightConditionsAgent::query).invoke(slotId)` to get `ConditionsReport`; (3) reject with `HttpResponses.badRequest("Flight conditions do not meet requirements")` if `!report.meetsRequirements()`; (4) call `componentClient.forEventSourcedEntity(BookingSlotEntity.class).call(slotId).method(BookingSlotEntity::bookSlot).invoke(new Command.BookReservation(request.studentId(), request.aircraftId(), request.instructorId(), request.bookingId()))`; (5) return `HttpResponses.created()`

**Checkpoint**: Run `mvn compile`. Test booking with good slot and bad slot (day 13). Verify `GET /flight/slots/alice/booked` returns the booking.

---

## Phase 5: User Story 3 — Cancel a Booking (P3)

**Goal**: A student can cancel an existing booking, freeing all participants.

**User Story covered**: US4 (cancel booking)

**Independent Test**: After a successful booking → `DELETE /flight/bookings/{slotId}/{bookingId}` returns `200`; `GET /flight/availability/{slotId}` shows empty bookings; `GET /flight/slots/alice/booked` returns empty list.

- [x] T008 [US3] Implement `FlightEndpoint.cancelBooking` in `src/main/java/io/example/api/FlightEndpoint.java`: call `componentClient.forEventSourcedEntity(BookingSlotEntity.class).call(slotId).method(BookingSlotEntity::cancelBooking).invoke(bookingId)` and return `HttpResponses.ok()`

**Checkpoint**: Run `mvn compile`. Book a slot then cancel it and verify all state is cleared.

---

## Phase 6: User Story 4 — Query Participant Slots by Status (P4)

**Goal**: Any participant can query their slots filtered by `available` or `booked` status.

**User Story covered**: US5 (query slots by participant + status)

**Independent Test**: After marking availability and booking → `GET /flight/slots/alice/available` returns available slots; `GET /flight/slots/alice/booked` returns booked slots.

- [x] T009 [US4] Implement `FlightEndpoint.slotsByStatus` in `src/main/java/io/example/api/FlightEndpoint.java`: call `componentClient.forView().method(ParticipantSlotsView::getSlotsByParticipantAndStatus).invoke(new ParticipantSlotsView.ParticipantStatusInput(participantId, status))` and return the result

**Checkpoint**: Run `mvn compile`. Query slots for a participant after multiple state changes and verify correct filtering.

---

## Phase 7: Polish & Validation

**Purpose**: Final compilation, full test run, and end-to-end scenario validation.

- [x] T010 Run `mvn verify` in project root — all tests must pass
- [ ] T011 Execute the happy-path scenario from `specs/001-flight-training-scheduler/quickstart.md` using curl against a locally-running service — verify all responses match expected output
- [ ] T012 [P] Execute the bad-weather scenario from `quickstart.md` (slot `2025-12-13-10`) — verify booking returns `400` with conditions message

---

## Dependencies & Execution Order

### Phase Dependencies

```
Phase 2 (Foundational)        ← no dependencies — start immediately
  ├── T001 BookingSlotEntity
  ├── T002 ParticipantSlotEntity  [parallel with T001]
  └── T003 FlightConditionsAgent  [parallel with T001, T002]

Phase 3 (US1 — Availability)  ← requires Phase 2 complete
  ├── T004 ParticipantSlotsView
  ├── T005 SlotToParticipantConsumer  [parallel with T004]
  └── T006 FlightEndpoint availability methods  [after T004 + T005]

Phase 4 (US2 — Booking)       ← requires Phase 3 complete
  └── T007 FlightEndpoint.createBooking

Phase 5 (US3 — Cancellation)  ← requires Phase 3 complete (parallel with Phase 4)
  └── T008 FlightEndpoint.cancelBooking

Phase 6 (US4 — Query)         ← requires Phase 3 complete (parallel with Phase 4, 5)
  └── T009 FlightEndpoint.slotsByStatus

Phase 7 (Polish)              ← requires all phases complete
  ├── T010 mvn verify
  ├── T011 quickstart happy path
  └── T012 quickstart bad weather  [parallel with T011]
```

### Parallel Opportunities

```bash
# Phase 2 — all three are different files, launch together:
T001: BookingSlotEntity
T002: ParticipantSlotEntity
T003: FlightConditionsAgent

# Phase 3 — T004 and T005 are different files:
T004: ParticipantSlotsView
T005: SlotToParticipantConsumer

# Phases 4, 5, 6 — after Phase 3, these touch different methods of FlightEndpoint:
# implement sequentially or carefully in parallel (same file — no race if different methods)
T007: createBooking
T008: cancelBooking  [parallel with T007 — different method]
T009: slotsByStatus  [parallel with T007/T008 — different method]
```

---

## Implementation Strategy

### MVP (Phases 2–3 only)

1. Complete Phase 2: T001 + T002 + T003 in parallel
2. Complete Phase 3: T004 + T005 in parallel, then T006
3. **Validate**: mark availability, retrieve slot state, query participant slots
4. MVP delivers mark/unmark availability + participant query

### Full Implementation

1. MVP (above) → then add T007 (booking) → T008 (cancel) → T009 (query by status)
2. Each step adds one HTTP route without touching previous routes
3. Finish with Phase 7 validation (T010–T012)

---

## Task Summary

| Phase | Tasks | Parallel Opportunities |
|-------|-------|----------------------|
| Phase 2: Foundational | T001–T003 (3 tasks) | All 3 parallel |
| Phase 3: Availability | T004–T006 (3 tasks) | T004+T005 parallel |
| Phase 4: Booking | T007 (1 task) | — |
| Phase 5: Cancellation | T008 (1 task) | Parallel with T007 |
| Phase 6: Query | T009 (1 task) | Parallel with T007/T008 |
| Phase 7: Polish | T010–T012 (3 tasks) | T011+T012 parallel |
| **Total** | **12 tasks** | **Up to 6 parallel** |
