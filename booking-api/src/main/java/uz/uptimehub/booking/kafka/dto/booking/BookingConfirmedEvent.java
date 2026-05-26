package uz.uptimehub.booking.kafka.dto.booking;

import lombok.*;
import uz.uptimehub.booking.kafka.dto.KafkaEvent;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Builder
public class BookingConfirmedEvent extends KafkaEvent {
    private UUID bookingId;
    private UUID resourceId;
    private UUID userId;
    private LocalDateTime confirmedAt;

    public BookingConfirmedEvent(UUID eventId, UUID bookingId, UUID resourceId, UUID userId, LocalDateTime confirmedAt) {
        super(eventId);
        this.bookingId = bookingId;
        this.resourceId = resourceId;
        this.userId = userId;
        this.confirmedAt = confirmedAt;
    }

}
