# Quickstart: Flight Training Scheduler

## Build & Run

```bash
# Compile
mvn compile

# Run all tests
mvn test

# Run integration tests
mvn verify

# Start locally (requires Akka local runtime)
mvn compile exec:java &   # or use: akka local run
```

## Test Scenario: Happy Path

Use slot `2025-12-10-10` (future date, good weather).

```bash
# 1. Mark availability for 3 participants
curl -s -H "Content-Type: application/json" -X POST \
  -d '{"participantId": "alice", "participantType": "student"}' \
  localhost:9000/flight/availability/2025-12-10-10

curl -s -H "Content-Type: application/json" -X POST \
  -d '{"participantId": "superplane", "participantType": "aircraft"}' \
  localhost:9000/flight/availability/2025-12-10-10

curl -s -H "Content-Type: application/json" -X POST \
  -d '{"participantId": "superteacher", "participantType": "instructor"}' \
  localhost:9000/flight/availability/2025-12-10-10

# 2. Check slot availability state
curl -s localhost:9000/flight/availability/2025-12-10-10

# 3. Query alice's available slots
curl -s localhost:9000/flight/slots/alice/available

# 4. Book the slot
curl -s -H "Content-Type: application/json" -X POST \
  -d '{"studentId":"alice","aircraftId":"superplane","instructorId":"superteacher","bookingId":"booking1"}' \
  localhost:9000/flight/bookings/2025-12-10-10

# 5. Check alice's booked slots
curl -s localhost:9000/flight/slots/alice/booked

# 6. Cancel the booking
curl -s -X DELETE localhost:9000/flight/bookings/2025-12-10-10/booking1

# 7. Verify slot is empty
curl -s localhost:9000/flight/availability/2025-12-10-10
```

## Test Scenario: Bad Weather (Day 13)

Use slot `2025-12-13-10` — the mock weather tool returns below-VFR conditions.

```bash
curl -s -H "Content-Type: application/json" -X POST \
  -d '{"participantId": "alice", "participantType": "student"}' \
  localhost:9000/flight/availability/2025-12-13-10

curl -s -H "Content-Type: application/json" -X POST \
  -d '{"participantId": "superplane", "participantType": "aircraft"}' \
  localhost:9000/flight/availability/2025-12-13-10

curl -s -H "Content-Type: application/json" -X POST \
  -d '{"participantId": "superteacher", "participantType": "instructor"}' \
  localhost:9000/flight/availability/2025-12-13-10

# This should return 400 — conditions not met
curl -s -H "Content-Type: application/json" -X POST \
  -d '{"studentId":"alice","aircraftId":"superplane","instructorId":"superteacher","bookingId":"booking2"}' \
  localhost:9000/flight/bookings/2025-12-13-10
```

## Weather Convention

| Day in slotId | Conditions |
|---|---|
| Any day **except 13** | Good VFR — visibility 10mi, ceiling 5000ft, wind 8kt |
| Day **13** | Poor — visibility 1mi, ceiling 400ft, wind 35kt |
