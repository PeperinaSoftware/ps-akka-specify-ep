# Feature Spec: Flight Training Scheduler

**Feature**: `001-flight-training-scheduler`
**Date**: 2026-04-23
**Status**: Implementation-ready

## Overview

Implement the backend for a flight training scheduling system. The scaffold, domain objects, and component stubs already exist. The task is to fill in the implementation of all Akka SDK components without modifying any domain objects.

## User Stories

- As a **student**, I want to mark my availability for a time slot so that I can be booked for training.
- As an **instructor**, I want to mark my availability so that students can book sessions with me.
- As an **aircraft owner/operator**, I want to register an aircraft as available for a time slot.
- As a **student**, I want to book a training flight by specifying the slot, instructor, and aircraft, knowing conditions will be verified.
- As a **student**, I want to cancel an existing booking so that the slot and resources become available again.
- As any **participant**, I want to query my upcoming availability and bookings by status.

## Requirements

### Functional

1. Participants (student, instructor, aircraft) can mark and unmark availability for a time slot.
2. A booking requires exactly one student, one instructor, and one aircraft — all must be marked available in the same slot.
3. Bookings can only be created for future time slots (slotId format: `YYYY-MM-DD-HH`).
4. Before confirming a booking, the `FlightConditionsAgent` must approve weather conditions for the slot.
5. A booking can be canceled at any time; cancellation removes all three participants from the booked state.
6. Participants can query their slots filtered by status (`available` or `booked`).

### Non-Functional

- No double-bookings: `BookingSlotEntity` enforces consistency at the entity level.
- Event-driven propagation: `SlotToParticipantConsumer` keeps `ParticipantSlotEntity` in sync.
- The `ParticipantSlotsView` provides the queryable read model.

## Components

| Component | Type | Key | Description |
|-----------|------|-----|-------------|
| `BookingSlotEntity` | Event Sourced Entity | `slotId` | Authority for a single time slot's availability and bookings |
| `FlightConditionsAgent` | Agent | session UUID | Evaluates VFR flight conditions for a given slot via LLM + mock weather tool |
| `ParticipantSlotEntity` | Event Sourced Entity | `{slotId}-{participantId}` | Per-participant view of a slot's status |
| `ParticipantSlotsView` | View | — | Queryable projection of participant slot statuses |
| `SlotToParticipantConsumer` | Consumer | — | Bridges `BookingSlotEntity` events → `ParticipantSlotEntity` commands |
| `FlightEndpoint` | HTTP Endpoint | — | Public REST API |

## Do Not Modify

- `io.example.domain.Timeslot`
- `io.example.domain.BookingEvent`
- `io.example.domain.Participant`
