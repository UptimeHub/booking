package uz.uptimehub.booking.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import uz.uptimehub.booking.dto.booking.BookingDto;
import uz.uptimehub.booking.dto.booking.Status;
import uz.uptimehub.booking.service.BookingAdminService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/booking")
@RequiredArgsConstructor
public class BookingAdminController {
    private final BookingAdminService bookingAdminService;

    @GetMapping
    public Page<BookingDto> searchBookings(
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) UUID resourceId,
            @RequestParam(required = false) Status status,
            @RequestParam(required = false) LocalDateTime from,
            @RequestParam(required = false) LocalDateTime to,
            Pageable pageable,
            HttpServletRequest request
    ) {
        UUID organizationId = bookingAdminService.assertOrganizationId(request);
        return bookingAdminService.searchBookings(
                organizationId,
                userId,
                resourceId,
                status,
                from,
                to,
                pageable
        );
    }

    @GetMapping("/current")
    public List<BookingDto> getCurrentBookingsForResource(
            @RequestParam UUID resourceId,
            HttpServletRequest request
    ) {
        UUID organizationId = bookingAdminService.assertOrganizationId(request);
        return bookingAdminService.getCurrentBookingsForResource(organizationId, resourceId);
    }

    @PostMapping("/{bookingId}/cancel")
    public BookingDto cancelBookingAsAdmin(@PathVariable UUID bookingId, HttpServletRequest request) {
        UUID organizationId = bookingAdminService.assertOrganizationId(request);
        return bookingAdminService.cancelBookingAsAdmin(organizationId, bookingId);
    }
}
