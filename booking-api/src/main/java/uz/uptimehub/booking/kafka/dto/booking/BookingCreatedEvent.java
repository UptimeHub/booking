package uz.uptimehub.booking.kafka.dto.booking;

import lombok.*;
import uz.uptimehub.booking.kafka.dto.KafkaEvent;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class BookingCreatedEvent extends KafkaEvent {
    private UUID bookingId;
    private UUID resourceId;
    private UUID userId;

    public BookingCreatedEvent(UUID eventId, UUID bookingId, UUID resourceId, UUID userId) {
        super(eventId);
        this.bookingId = bookingId;
        this.resourceId = resourceId;
        this.userId = userId;
    }
}
