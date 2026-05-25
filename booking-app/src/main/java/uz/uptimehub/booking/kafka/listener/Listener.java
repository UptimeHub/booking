package uz.uptimehub.booking.kafka.listener;

import uz.uptimehub.booking.kafka.dto.KafkaEvent;

public interface Listener<T extends KafkaEvent> {
    void receiveEvent(T event);
}
