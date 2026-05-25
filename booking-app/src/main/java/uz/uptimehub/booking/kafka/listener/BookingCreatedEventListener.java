package uz.uptimehub.booking.kafka.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import uz.uptimehub.booking.kafka.dto.booking.BookingCreatedEvent;
import uz.uptimehub.booking.service.BookingService;

@Component
@Slf4j
@RequiredArgsConstructor
public class BookingCreatedEventListener implements Listener<BookingCreatedEvent> {

    private final BookingService bookingService;

    @KafkaListener(
            topics = "${app.kafka.topics.consume.booking-create}",
            concurrency = "${app.kafka.concurrency}",
            groupId = "booking-booking-created"
    )
    @Override
    public void receiveEvent(BookingCreatedEvent event) {
        log.info("Received BookingCreatedEvent: {}", event);
        bookingService.processBookingEvent(event);
    }
}
