package uz.uptimehub.booking.dto.booking;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Request object for creating a new booking")
public record BookingCreateRequest(
        @Schema(description = "ID of the resource")
        @NotNull(message = "Resource ID is required")
        UUID resourceId,
        @Schema(description = "Starting time of the booking")
        @NotNull(message = "Starting time is required")
        LocalDateTime startTime,
        @Schema(description = "Ending time of the booking")
        @NotNull(message = "Ending time is required")
        LocalDateTime endTime
) {
}
