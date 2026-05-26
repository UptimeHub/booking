package uz.uptimehub.booking.kafka.producer;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import uz.uptimehub.booking.kafka.dto.booking.BookingConfirmedEvent;

@Component
public class BookingConfirmedEventProducer extends Producer<BookingConfirmedEvent> {
    public BookingConfirmedEventProducer(
            @Qualifier("bookingConfirmedEventKafkaTemplate")
            KafkaTemplate<String, BookingConfirmedEvent> kafkaTemplate,
            @Value("${app.kafka.topics.produce.booking-confirmed}")
            String bookingConfirmedTopic
    ) {
        super(kafkaTemplate, bookingConfirmedTopic);
    }
}
