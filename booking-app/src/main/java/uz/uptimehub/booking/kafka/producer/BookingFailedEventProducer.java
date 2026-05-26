package uz.uptimehub.booking.kafka.producer;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import uz.uptimehub.booking.kafka.dto.booking.BookingFailedEvent;

@Component
public class BookingFailedEventProducer extends Producer<BookingFailedEvent> {
    public BookingFailedEventProducer(
            @Qualifier("bookingFailedEventKafkaTemplate")
            KafkaTemplate<String, BookingFailedEvent> kafkaTemplate,
            @Value("${app.kafka.topics.produce.booking-failed}")
            String bookingFailedTopic
    ) {
        super(kafkaTemplate, bookingFailedTopic);
    }

}
