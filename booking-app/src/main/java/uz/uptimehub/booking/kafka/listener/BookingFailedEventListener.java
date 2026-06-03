package uz.uptimehub.booking.kafka.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import uz.uptimehub.booking.kafka.dto.booking.BookingFailedEvent;
import uz.uptimehub.booking.service.BookingService;

@Component
@Slf4j
@RequiredArgsConstructor
public class BookingFailedEventListener implements Listener<BookingFailedEvent> {

    private final BookingService bookingService;

    @KafkaListener(
            topics = "${app.kafka.topics.consume.booking-failed}",
            concurrency = "${app.kafka.concurrency}",
            groupId = "booking-booking-failed"
    )
    @Override
    public void receiveEvent(BookingFailedEvent event) {
        log.info("Received BookingFailedEvent: {}", event);
        bookingService.processBookingStatus(event);
    }
}
