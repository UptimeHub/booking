package uz.uptimehub.booking.kafka.dto.booking;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uz.uptimehub.booking.kafka.dto.KafkaEvent;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BookingCreatedEvent extends KafkaEvent {
    private UUID bookingId;
    private UUID resourceId;
    private UUID userId;
}
