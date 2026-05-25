package uz.uptimehub.booking.kafka.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class KafkaEvent {
    private UUID eventId;
}
