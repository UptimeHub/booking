package uz.uptimehub.booking.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.uptimehub.booking.dto.booking.BookingCreateRequest;
import uz.uptimehub.booking.dto.booking.BookingDto;
import uz.uptimehub.booking.dto.booking.Status;
import uz.uptimehub.booking.exception.CannotCreateBookingException;
import uz.uptimehub.booking.jpa.entity.Booking;
import uz.uptimehub.booking.jpa.repository.BookingRepository;
import uz.uptimehub.booking.kafka.dto.booking.BookingCreatedEvent;
import uz.uptimehub.booking.kafka.dto.booking.BookingFailedEvent;
import uz.uptimehub.booking.kafka.producer.BookingConfirmedEventProducer;
import uz.uptimehub.booking.kafka.producer.BookingCreatedEventProducer;
import uz.uptimehub.booking.kafka.producer.BookingFailedEventProducer;
import uz.uptimehub.booking.mapper.BookingMapper;
import uz.uptimehub.booking.utils.HeaderUtils;
import uz.uptimehub.core.exception.EntityNotFoundException;
import uz.uptimehub.resource.dto.client.ResourceClient;
import uz.uptimehub.resource.dto.resource.ResourceDto;
import uz.uptimehub.resource.dto.resource.ResourceStatus;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final BookingMapper bookingMapper;
    private final HeaderUtils headerUtils;
    private final ResourceClient resourceClient;
    private final BookingCreatedEventProducer bookingCreatedEventProducer;
    private final BookingFailedEventProducer bookingFailedEventProducer;
    private final BookingConfirmedEventProducer bookingConfirmedEventProducer;

    @Transactional("transactionManager")
    public BookingDto createBooking(BookingCreateRequest body, HttpServletRequest request) {
        ResourceDto resource = resourceClient.getById(body.resourceId());

        assertResourceIsActive(resource);
        assertBookingDatesCorrectness(body);

        UUID userId = headerUtils.extractUserId(request);

        if (userId == null) {
            throw new RuntimeException("User ID not found in request headers");
        }

        Booking booking = bookingMapper.toEntity(body, resource, userId);

        bookingRepository.save(booking);

        bookingCreatedEventProducer.send(
                new BookingCreatedEvent(UUID.randomUUID(), booking.getId(), resource.getId(), userId),
                null
        );

        return bookingMapper.toDto(booking);

    }

    public void processBookingEvent(BookingCreatedEvent event) {
        Booking booking = bookingRepository.findById(event.getBookingId()).orElseThrow();

        boolean isBooked = bookingRepository.existsByResourceIdAndStatusAndStartTimeLessThanEqualAndEndTimeGreaterThanEqual(
                booking.getResourceId(),
                Status.ACTIVE,
                booking.getEndTime(),
                booking.getStartTime()
        );

        if (isBooked) {
            booking.setStatus(Status.FAILED);
            bookingFailedEventProducer.send(
                    new BookingFailedEvent(
                            UUID.randomUUID(),
                            booking.getId(),
                            booking.getResourceId(),
                            booking.getUserId(),
                            "Resource is already booked for the given time range",
                            LocalDateTime.now()
                    ),
                    null
            );
        } else {
            booking.setStatus(Status.ACTIVE);
            bookingCreatedEventProducer.send(
                    new BookingCreatedEvent(
                            UUID.randomUUID(),
                            booking.getId(),
                            booking.getResourceId(),
                            booking.getUserId()
                    ),
                    null
            );
        }

        bookingRepository.save(booking);
    }

    @Transactional(transactionManager = "transactionManager")
    @Scheduled(fixedRate = 60000) // every minute
    public void expireOldBookings() {
        log.info("Checking for old bookings, and mark their status expired");
        bookingRepository.markExpiredBookings(Status.ACTIVE, Status.EXPIRED, LocalDateTime.now());
    }

    private void assertResourceIsActive(ResourceDto resource) {
        if (resource == null) {
            throw new EntityNotFoundException("Resource not found");
        }

        if (resource.getStatus() != ResourceStatus.PUBLISHED) {
            throw new CannotCreateBookingException("Cannot create booking for resource that is not active");
        }
    }

    private void assertBookingDatesCorrectness(BookingCreateRequest body) {
        if (body.startTime().isAfter(body.endTime()) || body.startTime().isEqual(body.endTime())) {
            throw new CannotCreateBookingException("Start time must be before end time");
        }
    }
}
