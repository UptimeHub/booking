package uz.uptimehub.booking.kafka.producer;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import uz.uptimehub.booking.kafka.dto.booking.BookingCreatedEvent;

@Component
public class BookingCreatedEventProducer extends Producer<BookingCreatedEvent>{
    public BookingCreatedEventProducer(
            @Qualifier("bookingCreatedEventKafkaTemplate")
            KafkaTemplate<String, BookingCreatedEvent> kafkaTemplate,
            @Value("${app.kafka.topics.produce.booking-create}")
            String bookingCreatedTopic
    ) {
        super(kafkaTemplate, bookingCreatedTopic);
    }

}
