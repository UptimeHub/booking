package uz.uptimehub.booking.kafka.dto;

import java.util.UUID;

public record BookingCreatedEvent(
        UUID bookingId,
        UUID resourceId,
        UUID userId
) {
}
