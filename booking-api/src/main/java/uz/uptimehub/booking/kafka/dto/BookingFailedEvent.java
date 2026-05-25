package uz.uptimehub.booking.kafka.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record BookingFailedEvent(
        UUID bookingId,
        UUID resourceId,
        UUID userId,
        String reason,
        LocalDateTime failedAt
) {
}
