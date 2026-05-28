package uz.uptimehub.booking.integration.kafka;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import uz.uptimehub.booking.dto.booking.Status;
import uz.uptimehub.booking.integration.BaseIntegrationTest;
import uz.uptimehub.booking.jpa.entity.Booking;
import uz.uptimehub.booking.jpa.repository.BookingRepository;
import uz.uptimehub.booking.kafka.dto.booking.BookingCreatedEvent;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.awaitility.Awaitility.await;

public class BookingKafkaIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private KafkaTemplate<String, BookingCreatedEvent> bookingCreatedKafkaTemplate;

    @Value("${app.kafka.topics.consume.booking-create}")
    private String bookingCreatedTopic;

    @BeforeEach
    void cleanDatabase() {
        bookingRepository.deleteAll();
    }

    @Test
    @DisplayName("Should update booking status to active when BookingCreatedEvent is consumed")
    void shouldUpdateBookingStatusWhenCreatedEventConsumed() {
        UUID eventId = UUID.randomUUID();
        UUID resourceId = UUID.randomUUID();
        UUID organizationId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Booking booking = Booking.builder()
                .resourceId(resourceId)
                .organizationId(organizationId)
                .userId(userId)
                .startTime(LocalDateTime.parse("2026-05-28T10:00:00"))
                .endTime(LocalDateTime.parse("2026-05-28T11:00:00"))
                .status(Status.PENDING)
                .build();

        Booking savedBooking = bookingRepository.saveAndFlush(booking);
        BookingCreatedEvent event = new BookingCreatedEvent(
                eventId,
                savedBooking.getId(),
                resourceId,
                userId
        );

        bookingCreatedKafkaTemplate.executeInTransaction(kafkaOperations -> {
            kafkaOperations.send(bookingCreatedTopic, event);
            return null;
        });

        await()
                .atMost(Duration.ofSeconds(10))
                .untilAsserted(() -> {
                    Booking updated = bookingRepository.findById(savedBooking.getId()).orElseThrow();
                    assertThat(updated.getStatus()).isEqualTo(Status.ACTIVE);
                });

    }
}
