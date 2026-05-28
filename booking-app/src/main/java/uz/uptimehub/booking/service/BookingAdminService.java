package uz.uptimehub.booking.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.uptimehub.booking.dto.booking.BookingDto;
import uz.uptimehub.booking.dto.booking.Status;
import uz.uptimehub.booking.jpa.entity.Booking;
import uz.uptimehub.booking.jpa.repository.BookingRepository;
import uz.uptimehub.booking.mapper.BookingMapper;
import uz.uptimehub.booking.utils.HeaderUtils;
import uz.uptimehub.core.exception.EntityNotFoundException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookingAdminService {

    private final BookingRepository bookingRepository;
    private final BookingMapper bookingMapper;
    private final HeaderUtils headerUtils;

    public Page<BookingDto> searchBookings(
            UUID organizationId,
            UUID userId,
            UUID resourceId,
            Status status,
            LocalDateTime from,
            LocalDateTime to,
            Pageable pageable
    ) {
        return bookingRepository.searchBookings(organizationId, userId, resourceId, status, from, to, pageable)
                .map(bookingMapper::toDto);
    }

    public List<BookingDto> getCurrentBookingsForResource(UUID organizationId, UUID resourceId) {
        return bookingRepository.findCurrentBookingsForResource(
                        organizationId,
                        resourceId,
                        Status.ACTIVE,
                        LocalDateTime.now()
                )
                .stream()
                .map(bookingMapper::toDto)
                .toList();
    }

    @Transactional("transactionManager")
    public BookingDto cancelBookingAsAdmin(UUID organizationId, UUID bookingId) {
        Booking booking = bookingRepository.findByIdAndOrganizationId(bookingId, organizationId)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found"));

        if (booking.getStatus() == Status.CANCELLED
                || booking.getStatus() == Status.EXPIRED
                || booking.getStatus() == Status.FAILED) {
            throw new IllegalStateException("Booking cannot be cancelled");
        }

        booking.setStatus(Status.CANCELLED);
        return bookingMapper.toDto(bookingRepository.save(booking));
    }

    public UUID assertOrganizationId(HttpServletRequest request) {
        UUID organizationId = headerUtils.extractOrganizationId(request);

        if (organizationId == null) {
            throw new RuntimeException("Organization ID not found in request headers");
        }

        return organizationId;
    }
}
