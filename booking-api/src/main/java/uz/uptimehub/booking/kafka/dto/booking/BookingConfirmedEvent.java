package uz.uptimehub.booking.kafka.dto.booking;

import lombok.*;
import uz.uptimehub.booking.kafka.dto.KafkaEvent;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BookingConfirmedEvent extends KafkaEvent {
    private UUID bookingId;
    private UUID resourceId;
    private UUID userId;
    private LocalDateTime confirmedAt;

}
