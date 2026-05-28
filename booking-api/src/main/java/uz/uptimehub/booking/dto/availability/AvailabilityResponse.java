package uz.uptimehub.booking.dto.availability;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Schema(description = "Availability timeline for a selected resource and time range")
public record AvailabilityResponse(
        @Schema(description = "ID of the selected resource")
        UUID resourceId,
        @Schema(description = "Start time of the checked range")
        LocalDateTime from,
        @Schema(description = "End time of the checked range")
        LocalDateTime to,
        @Schema(description = "Continuous available and unavailable periods inside the checked range")
        List<AvailabilityPeriodDto> periods
) {
}
