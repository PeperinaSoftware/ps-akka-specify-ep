package io.example.application;

import akka.javasdk.annotations.Component;
import akka.javasdk.annotations.Consume;
import akka.javasdk.client.ComponentClient;
import akka.javasdk.consumer.Consumer;
import io.example.domain.BookingEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// This class is responsible for consuming events from the booking
// slot entity and turning those into command calls on the
// participant slot entity
@Component(id = "booking-slot-consumer")
@Consume.FromEventSourcedEntity(BookingSlotEntity.class)
public class SlotToParticipantConsumer extends Consumer {

    private final ComponentClient client;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public SlotToParticipantConsumer(ComponentClient client) {
        this.client = client;
    }

    public Effect onEvent(BookingEvent event) {
        switch (event) {
            case BookingEvent.ParticipantMarkedAvailable evt -> {
                logger.info("Marking available {} for participant {}", evt.slotId(), evt.participantId());
                client.forEventSourcedEntity(participantSlotId(evt))
                        .method(ParticipantSlotEntity::markAvailable)
                        .invoke(new ParticipantSlotEntity.Commands.MarkAvailable(
                                evt.slotId(), evt.participantId(), evt.participantType()));
            }
            case BookingEvent.ParticipantUnmarkedAvailable evt -> {
                logger.info("Unmarking available {} for participant {}", evt.slotId(), evt.participantId());
                client.forEventSourcedEntity(participantSlotId(evt))
                        .method(ParticipantSlotEntity::unmarkAvailable)
                        .invoke(new ParticipantSlotEntity.Commands.UnmarkAvailable(
                                evt.slotId(), evt.participantId(), evt.participantType()));
            }
            case BookingEvent.ParticipantBooked evt -> {
                logger.info("Booking {} for participant {}", evt.slotId(), evt.participantId());
                client.forEventSourcedEntity(participantSlotId(evt))
                        .method(ParticipantSlotEntity::book)
                        .invoke(new ParticipantSlotEntity.Commands.Book(
                                evt.slotId(), evt.participantId(), evt.participantType(), evt.bookingId()));
            }
            case BookingEvent.ParticipantCanceled evt -> {
                logger.info("Canceling booking {} for participant {}", evt.bookingId(), evt.participantId());
                client.forEventSourcedEntity(participantSlotId(evt))
                        .method(ParticipantSlotEntity::cancel)
                        .invoke(new ParticipantSlotEntity.Commands.Cancel(
                                evt.slotId(), evt.participantId(), evt.participantType(), evt.bookingId()));
            }
        }
        return effects().done();
    }

    // Participant slots are keyed by a derived key made up of
    // {slotId}-{participantId}
    // We don't need the participant type here because the participant IDs
    // should always be unique/UUIDs
    private String participantSlotId(BookingEvent event) {
        return switch (event) {
            case BookingEvent.ParticipantBooked evt -> evt.slotId() + "-" + evt.participantId();
            case BookingEvent.ParticipantUnmarkedAvailable evt ->
                evt.slotId() + "-" + evt.participantId();
            case BookingEvent.ParticipantMarkedAvailable evt -> evt.slotId() + "-" + evt.participantId();
            case BookingEvent.ParticipantCanceled evt -> evt.slotId() + "-" + evt.participantId();
        };
    }
}
