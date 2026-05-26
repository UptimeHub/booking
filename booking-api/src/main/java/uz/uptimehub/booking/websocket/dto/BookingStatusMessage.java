package uz.uptimehub.booking.websocket.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BookingStatusMessage {
    private UUID bookingId;
    private UUID resourceId;
    private String status;
    private String reason;
    private LocalDateTime occurredAt;
}
