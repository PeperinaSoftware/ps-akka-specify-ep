package io.example.api;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.http.javadsl.model.HttpResponse;
import akka.javasdk.annotations.Acl;
import akka.javasdk.annotations.http.Delete;
import akka.javasdk.annotations.http.Get;
import akka.javasdk.annotations.http.HttpEndpoint;
import akka.javasdk.annotations.http.Post;
import akka.javasdk.client.ComponentClient;
import akka.javasdk.http.AbstractHttpEndpoint;
import akka.javasdk.http.HttpException;
import akka.javasdk.http.HttpResponses;
import io.example.application.BookingSlotEntity;
import io.example.application.BookingSlotEntity.Command;
import io.example.application.FlightConditionsAgent;
import io.example.application.ParticipantSlotsView;
import io.example.application.ParticipantSlotsView.SlotList;
import io.example.domain.Participant;
import io.example.domain.Participant.ParticipantType;
import io.example.domain.Timeslot;

@Acl(allow = @Acl.Matcher(principal = Acl.Principal.INTERNET))
@HttpEndpoint("/flight")
public class FlightEndpoint extends AbstractHttpEndpoint {
    private final Logger log = LoggerFactory.getLogger(FlightEndpoint.class);

    private static final DateTimeFormatter SLOT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH");

    private final ComponentClient componentClient;

    public FlightEndpoint(ComponentClient componentClient) {
        this.componentClient = componentClient;
    }

    // Creates a new booking. All three identified participants will
    // be considered booked for the given timeslot, if they are all
    // "available" at the time of booking.
    @Post("/bookings/{slotId}")
    public HttpResponse createBooking(String slotId, BookingRequest request) {
        log.info("Creating booking for slot {}: {}", slotId, request);

        // Reject slots in the past
        LocalDateTime slotTime;
        try {
            slotTime = LocalDateTime.parse(slotId, SLOT_FORMATTER);
        } catch (Exception e) {
            throw HttpException.badRequest("Invalid slot ID format. Expected YYYY-MM-DD-HH");
        }
        if (!slotTime.isAfter(LocalDateTime.now())) {
            return HttpResponses.badRequest("Slot is in the past");
        }

        // Check flight conditions via agent
        var report = componentClient
                .forAgent()
                .inSession(UUID.randomUUID().toString())
                .method(FlightConditionsAgent::query)
                .invoke(slotId);

        if (!report.meetsRequirements()) {
            return HttpResponses.badRequest("Flight conditions do not meet VFR requirements for slot " + slotId);
        }

        // Book the slot
        componentClient
                .forEventSourcedEntity(slotId)
                .method(BookingSlotEntity::bookSlot)
                .invoke(new Command.BookReservation(
                        request.studentId(), request.aircraftId(), request.instructorId(), request.bookingId()));

        return HttpResponses.created();
    }

    // Cancels an existing booking. Note that both the slot
    // ID and the booking ID are required.
    @Delete("/bookings/{slotId}/{bookingId}")
    public HttpResponse cancelBooking(String slotId, String bookingId) {
        log.info("Canceling booking id {}", bookingId);

        componentClient
                .forEventSourcedEntity(slotId)
                .method(BookingSlotEntity::cancelBooking)
                .invoke(bookingId);

        return HttpResponses.ok();
    }

    // Retrieves all slots in which a given participant has the supplied status.
    // Used to retrieve bookings and slots in which the participant is available
    @Get("/slots/{participantId}/{status}")
    public SlotList slotsByStatus(String participantId, String status) {
        return componentClient
                .forView()
                .method(ParticipantSlotsView::getSlotsByParticipantAndStatus)
                .invoke(new ParticipantSlotsView.ParticipantStatusInput(participantId, status));
    }

    // Returns the internal availability state for a given slot
    @Get("/availability/{slotId}")
    public Timeslot getSlot(String slotId) {
        return componentClient
                .forEventSourcedEntity(slotId)
                .method(BookingSlotEntity::getSlot)
                .invoke();
    }

    // Indicates that the supplied participant is available for booking
    // within the indicated time slot
    @Post("/availability/{slotId}")
    public HttpResponse markAvailable(String slotId, AvailabilityRequest request) {
        ParticipantType participantType;

        try {
            participantType = ParticipantType.valueOf(request.participantType().trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            log.warn("Bad participant type {}", request.participantType());
            throw HttpException.badRequest("invalid participant type");
        }

        log.info("Marking timeslot available for entity {}", slotId);

        componentClient
                .forEventSourcedEntity(slotId)
                .method(BookingSlotEntity::markSlotAvailable)
                .invoke(new Command.MarkSlotAvailable(new Participant(request.participantId(), participantType)));

        return HttpResponses.ok();
    }

    // Unmarks a slot as available for the given participant.
    @Delete("/availability/{slotId}")
    public HttpResponse unmarkAvailable(String slotId, AvailabilityRequest request) {
        ParticipantType participantType;
        try {
            participantType = ParticipantType.valueOf(request.participantType().trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            log.warn("Bad participant type {}", request.participantType());
            throw HttpException.badRequest("invalid participant type");
        }

        componentClient
                .forEventSourcedEntity(slotId)
                .method(BookingSlotEntity::unmarkSlotAvailable)
                .invoke(new Command.UnmarkSlotAvailable(new Participant(request.participantId(), participantType)));

        return HttpResponses.ok();
    }

    // Public API representation of a booking request
    public record BookingRequest(
            String studentId, String aircraftId, String instructorId, String bookingId) {
    }

    // Public API representation of an availability mark/unmark request
    public record AvailabilityRequest(String participantId, String participantType) {
    }
}
