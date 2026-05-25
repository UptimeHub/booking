package uz.uptimehub.booking.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.uptimehub.booking.dto.booking.BookingCreateRequest;
import uz.uptimehub.booking.dto.booking.BookingDto;
import uz.uptimehub.booking.exception.CannotCreateBookingException;
import uz.uptimehub.booking.jpa.entity.Booking;
import uz.uptimehub.booking.jpa.repository.BookingRepository;
import uz.uptimehub.booking.kafka.dto.booking.BookingCreatedEvent;
import uz.uptimehub.booking.kafka.producer.BookingCreatedEventProducer;
import uz.uptimehub.booking.mapper.BookingMapper;
import uz.uptimehub.booking.utils.HeaderUtils;
import uz.uptimehub.core.exception.EntityNotFoundException;
import uz.uptimehub.resource.dto.client.ResourceClient;
import uz.uptimehub.resource.dto.resource.ResourceDto;
import uz.uptimehub.resource.dto.resource.ResourceStatus;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final BookingMapper bookingMapper;
    private final HeaderUtils headerUtils;
    private final ResourceClient resourceClient;
    private final BookingCreatedEventProducer bookingCreatedEventProducer;

    @Transactional
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
                new BookingCreatedEvent(booking.getId(), resource.getId(), userId),
                null
        );

        return bookingMapper.toDto(booking);

    }

    public void processBookingEvent(BookingCreatedEvent event) {
        //TODO process booking created event, e.g. send notification to resource owner
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
