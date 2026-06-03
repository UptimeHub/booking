package uz.uptimehub.booking.kafka.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import uz.uptimehub.booking.kafka.dto.booking.BookingConfirmedEvent;
import uz.uptimehub.booking.service.BookingService;

@Component
@Slf4j
@RequiredArgsConstructor
public class BookingConfirmedEventListener implements Listener<BookingConfirmedEvent> {

    private final BookingService bookingService;

    @KafkaListener(
            topics = "${app.kafka.topics.consume.booking-confirmed}",
            concurrency = "${app.kafka.concurrency}",
            groupId = "booking-booking-confirmed"
    )
    @Override
    public void receiveEvent(BookingConfirmedEvent event) {
        log.info("Received BookingConfirmedEvent: {}", event);
        bookingService.processBookingStatus(event);
    }
}
