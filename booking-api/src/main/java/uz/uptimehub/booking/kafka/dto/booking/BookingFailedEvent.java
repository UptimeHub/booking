package uz.uptimehub.booking.kafka.dto.booking;

import lombok.*;
import uz.uptimehub.booking.kafka.dto.KafkaEvent;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class BookingFailedEvent extends KafkaEvent {
    private UUID bookingId;
    private UUID resourceId;
    private UUID userId;
    private String reason;
    private LocalDateTime failedAt;

    public BookingFailedEvent(UUID eventId, UUID bookingId, UUID resourceId, UUID userId, String reason, LocalDateTime failedAt) {
        super(eventId);
        this.bookingId = bookingId;
        this.resourceId = resourceId;
        this.userId = userId;
        this.reason = reason;
        this.failedAt = failedAt;
    }
}
