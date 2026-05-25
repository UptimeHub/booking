package uz.uptimehub.booking.kafka.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record BookingConfirmedEvent(
        UUID bookingId,
        UUID resourceId,
        UUID userId,
        LocalDateTime confirmedAt
) {
}
