package uz.uptimehub.booking.dto.availability;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "A continuous availability period for a resource")
public record AvailabilityPeriodDto(
        @Schema(description = "Start time of the period")
        LocalDateTime startTime,
        @Schema(description = "End time of the period")
        LocalDateTime endTime,
        @Schema(description = "Availability status for the period")
        AvailabilityStatus status
) {
}
