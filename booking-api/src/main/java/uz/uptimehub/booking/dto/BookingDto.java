package uz.uptimehub.booking.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Data transfer object representing a booking with its basic information")
public record BookingDto(
        @Schema(description = "ID of the booking")
        UUID id,
        @Schema(description = "ID of the resource")
        UUID resourceId,
        @Schema(description = "ID of the organization that owns the resource")
        UUID organizationId,
        @Schema(description = "ID of the user who made the booking")
        UUID userId,
        @Schema(description = "Start time of the booking")
        LocalDateTime startTime,
        @Schema(description = "End time of the booking")
        LocalDateTime endTime,
        @Schema(description = "Status of the booking")
        Status status,
        @Schema(description = "Creation time of the booking")
        LocalDateTime createdAt,
        @Schema(description = "Last update time of the booking")
        LocalDateTime updatedAt
) {
}
