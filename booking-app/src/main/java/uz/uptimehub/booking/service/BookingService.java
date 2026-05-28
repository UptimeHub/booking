package uz.uptimehub.booking.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.uptimehub.booking.dto.availability.AvailabilityPeriodDto;
import uz.uptimehub.booking.dto.availability.AvailabilityResponse;
import uz.uptimehub.booking.dto.availability.AvailabilityStatus;
import uz.uptimehub.booking.dto.booking.BookingCreateRequest;
import uz.uptimehub.booking.dto.booking.BookingDto;
import uz.uptimehub.booking.dto.booking.Status;
import uz.uptimehub.booking.exception.CannotCreateBookingException;
import uz.uptimehub.booking.jpa.entity.Booking;
import uz.uptimehub.booking.jpa.repository.BookingRepository;
import uz.uptimehub.booking.kafka.dto.KafkaEvent;
import uz.uptimehub.booking.kafka.dto.booking.BookingConfirmedEvent;
import uz.uptimehub.booking.kafka.dto.booking.BookingCreatedEvent;
import uz.uptimehub.booking.kafka.dto.booking.BookingFailedEvent;
import uz.uptimehub.booking.kafka.producer.BookingConfirmedEventProducer;
import uz.uptimehub.booking.kafka.producer.BookingCreatedEventProducer;
import uz.uptimehub.booking.kafka.producer.BookingFailedEventProducer;
import uz.uptimehub.booking.mapper.BookingMapper;
import uz.uptimehub.booking.utils.HeaderUtils;
import uz.uptimehub.booking.websocket.dto.BookingStatusMessage;
import uz.uptimehub.core.exception.EntityNotFoundException;
import uz.uptimehub.resource.dto.client.ResourceClient;
import uz.uptimehub.resource.dto.resource.ResourceDto;
import uz.uptimehub.resource.dto.resource.ResourceStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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
    private final SimpMessagingTemplate messagingTemplate;

    private static final List<Status> BLOCKING_AVAILABILITY_STATUSES = List.of(Status.PENDING, Status.ACTIVE);

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

    public Page<BookingDto> getMyBookings(Status status, UUID resourceId, LocalDateTime from, LocalDateTime to, Pageable pageable, HttpServletRequest request) {
        UUID userId = headerUtils.extractUserId(request);
        Specification<Booking> specification = myBookingsSpecification(userId, status, resourceId, from, to);
        return bookingRepository.findAll(specification, pageable)
                .map(bookingMapper::toDto);
    }

    public BookingDto getBooking(UUID bookingId, HttpServletRequest request) {
        UUID userId = headerUtils.extractUserId(request);
        return bookingRepository.findByIdAndUserId(bookingId, userId)
                .map(bookingMapper::toDto)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found"));
    }

    public AvailabilityResponse getAvailability(UUID resourceId, LocalDateTime from, LocalDateTime to) {
        assertAvailabilityDatesCorrectness(from, to);

        ResourceDto resource = resourceClient.getById(resourceId);

        if (resource == null) {
            throw new EntityNotFoundException("Resource not found");
        }

        if (resource.getStatus() != ResourceStatus.PUBLISHED) {
            return new AvailabilityResponse(
                    resourceId,
                    from,
                    to,
                    List.of(new AvailabilityPeriodDto(from, to, AvailabilityStatus.UNAVAILABLE))
            );
        }

        List<Booking> blockingBookings = bookingRepository.findBlockingBookingsForResource(
                resourceId,
                BLOCKING_AVAILABILITY_STATUSES,
                from,
                to
        );

        return new AvailabilityResponse(
                resourceId,
                from,
                to,
                buildAvailabilityPeriods(from, to, blockingBookings)
        );
    }

    @Transactional("transactionManager")
    public BookingDto cancelMyBooking(UUID bookingId, HttpServletRequest request) {
        UUID userId = headerUtils.extractUserId(request);
        Booking booking = bookingRepository.findByIdAndUserId(bookingId, userId)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found"));

        if (booking.getStatus() == Status.CANCELLED
                || booking.getStatus() == Status.EXPIRED
                || booking.getStatus() == Status.FAILED) {
            throw new IllegalStateException("Booking cannot be cancelled");
        }

        booking.setStatus(Status.CANCELLED);
        return bookingMapper.toDto(bookingRepository.save(booking));
    }

    public void processBookingEvent(BookingCreatedEvent event) {
        Booking booking = bookingRepository.findById(event.getBookingId()).orElseThrow();

        boolean isBooked = bookingRepository.existsOverlappingBooking(
                booking.getResourceId(),
                Status.ACTIVE,
                booking.getStartTime(),
                booking.getEndTime()
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
            bookingConfirmedEventProducer.send(
                    new BookingConfirmedEvent(
                            UUID.randomUUID(),
                            booking.getId(),
                            booking.getResourceId(),
                            booking.getUserId(),
                            LocalDateTime.now()
                    ),
                    null
            );
        }

        bookingRepository.save(booking);
    }

    public void processBookingStatus(KafkaEvent event) {
        BookingStatusMessage statusMessage = toStatusMessage(event);

        messagingTemplate.convertAndSendToUser(
                statusMessage.getUserId().toString(),
                "/queue/booking-status",
                statusMessage
        );
    }

    private Specification<Booking> myBookingsSpecification(
            UUID userId,
            Status status,
            UUID resourceId,
            LocalDateTime from,
            LocalDateTime to
    ) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("userId"), userId));

            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }
            if (resourceId != null) {
                predicates.add(criteriaBuilder.equal(root.get("resourceId"), resourceId));
            }
            if (from != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("startTime"), from));
            }
            if (to != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("endTime"), to));
            }

            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
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

    private void assertAvailabilityDatesCorrectness(LocalDateTime from, LocalDateTime to) {
        if (from.isAfter(to) || from.isEqual(to)) {
            throw new IllegalArgumentException("From time must be before to time");
        }
    }

    private List<AvailabilityPeriodDto> buildAvailabilityPeriods(
            LocalDateTime from,
            LocalDateTime to,
            List<Booking> blockingBookings
    ) {
        List<AvailabilityPeriodDto> periods = new ArrayList<>();
        LocalDateTime cursor = from;
        LocalDateTime unavailableStart = null;
        LocalDateTime unavailableEnd = null;

        for (Booking booking : blockingBookings) {
            LocalDateTime bookingStart = max(booking.getStartTime(), from);
            LocalDateTime bookingEnd = min(booking.getEndTime(), to);

            if (!bookingStart.isBefore(bookingEnd)) {
                continue;
            }

            if (unavailableStart == null) {
                unavailableStart = bookingStart;
                unavailableEnd = bookingEnd;
                continue;
            }

            if (!bookingStart.isAfter(unavailableEnd)) {
                unavailableEnd = max(unavailableEnd, bookingEnd);
                continue;
            }

            cursor = addAvailabilityBlock(periods, cursor, unavailableStart, unavailableEnd);
            unavailableStart = bookingStart;
            unavailableEnd = bookingEnd;
        }

        if (unavailableStart != null) {
            cursor = addAvailabilityBlock(periods, cursor, unavailableStart, unavailableEnd);
        }

        if (cursor.isBefore(to)) {
            periods.add(new AvailabilityPeriodDto(cursor, to, AvailabilityStatus.AVAILABLE));
        }

        return periods;
    }

    private LocalDateTime addAvailabilityBlock(
            List<AvailabilityPeriodDto> periods,
            LocalDateTime cursor,
            LocalDateTime unavailableStart,
            LocalDateTime unavailableEnd
    ) {
        if (cursor.isBefore(unavailableStart)) {
            periods.add(new AvailabilityPeriodDto(cursor, unavailableStart, AvailabilityStatus.AVAILABLE));
        }

        periods.add(new AvailabilityPeriodDto(unavailableStart, unavailableEnd, AvailabilityStatus.UNAVAILABLE));
        return unavailableEnd;
    }

    private LocalDateTime max(LocalDateTime first, LocalDateTime second) {
        return first.isAfter(second) ? first : second;
    }

    private LocalDateTime min(LocalDateTime first, LocalDateTime second) {
        return first.isBefore(second) ? first : second;
    }

    private BookingStatusMessage toStatusMessage(KafkaEvent event) {
        if (event instanceof BookingConfirmedEvent confirmedEvent) {
            return BookingStatusMessage.builder()
                    .bookingId(confirmedEvent.getBookingId())
                    .userId(confirmedEvent.getUserId())
                    .resourceId(confirmedEvent.getResourceId())
                    .status("CONFIRMED")
                    .occurredAt(confirmedEvent.getConfirmedAt())
                    .build();
        } else if (event instanceof BookingFailedEvent failedEvent) {
            return BookingStatusMessage.builder()
                    .bookingId(failedEvent.getBookingId())
                    .userId(failedEvent.getUserId())
                    .resourceId(failedEvent.getResourceId())
                    .status("FAILED")
                    .reason(failedEvent.getReason())
                    .occurredAt(failedEvent.getFailedAt())
                    .build();
        } else {
            throw new RuntimeException("Unsupported event type: " + event.getClass().getName());
        }
    }
}
