package io.example.application;

import akka.javasdk.annotations.Component;
import akka.javasdk.annotations.Consume;
import akka.javasdk.annotations.Query;
import akka.javasdk.view.TableUpdater;
import akka.javasdk.view.View;
import io.example.application.ParticipantSlotEntity.Event.Booked;
import io.example.application.ParticipantSlotEntity.Event.Canceled;
import io.example.application.ParticipantSlotEntity.Event.MarkedAvailable;
import io.example.application.ParticipantSlotEntity.Event.UnmarkedAvailable;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(id = "view-participant-slots")
public class ParticipantSlotsView extends View {

    private static Logger logger = LoggerFactory.getLogger(ParticipantSlotsView.class);

    @Consume.FromEventSourcedEntity(ParticipantSlotEntity.class)
    public static class ParticipantSlotsViewUpdater extends TableUpdater<SlotRow> {

        public Effect<SlotRow> onEvent(ParticipantSlotEntity.Event event) {
            return switch (event) {
                case MarkedAvailable e -> effects().updateRow(
                        new SlotRow(e.slotId(), e.participantId(), e.participantType().name(), Optional.empty(), "available"));
                case UnmarkedAvailable e -> effects().deleteRow();
                case Booked e -> effects().updateRow(
                        new SlotRow(e.slotId(), e.participantId(), e.participantType().name(), Optional.of(e.bookingId()), "booked"));
                case Canceled e -> effects().updateRow(
                        new SlotRow(e.slotId(), e.participantId(), e.participantType().name(), Optional.of(e.bookingId()), "canceled"));
            };
        }
    }

    public record SlotRow(
            String slotId,
            String participantId,
            String participantType,
            Optional<String> bookingId,
            String status) {
    }

    public record ParticipantStatusInput(String participantId, String status) {
    }

    public record SlotList(List<SlotRow> slots) {
    }

    @Query("SELECT * AS slots FROM participant_slots_view WHERE participantId = :participantId")
    public QueryEffect<SlotList> getSlotsByParticipant(String participantId) {
        return queryResult();
    }

    @Query("SELECT * AS slots FROM participant_slots_view WHERE participantId = :participantId AND status = :status")
    public QueryEffect<SlotList> getSlotsByParticipantAndStatus(ParticipantStatusInput input) {
        return queryResult();
    }
}
