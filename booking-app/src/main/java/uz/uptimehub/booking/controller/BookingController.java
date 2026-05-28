package uz.uptimehub.booking.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import uz.uptimehub.booking.dto.availability.AvailabilityResponse;
import uz.uptimehub.booking.dto.booking.BookingCreateRequest;
import uz.uptimehub.booking.dto.booking.BookingDto;
import uz.uptimehub.booking.dto.booking.Status;
import uz.uptimehub.booking.service.BookingService;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/booking")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public BookingDto create(@Valid @RequestBody BookingCreateRequest body, HttpServletRequest request) {
        return bookingService.createBooking(body, request);
    }

    @GetMapping("/my")
    public Page<BookingDto> getMyBookings(
            @RequestParam(required = false) Status status,
            @RequestParam(required = false) UUID resourceId,
            @RequestParam(required = false) LocalDateTime from,
            @RequestParam(required = false) LocalDateTime to,
            HttpServletRequest request,
            Pageable pageable
    ) {
        return bookingService.getMyBookings(status, resourceId, from, to, pageable, request);
    }

    @GetMapping("/{bookingId}")
    public BookingDto getBooking(@PathVariable UUID bookingId, HttpServletRequest request) {
        return bookingService.getBooking(bookingId, request);
    }

    @GetMapping("/resource/availability/{resourceId}")
    public AvailabilityResponse getAvailability(
            @PathVariable UUID resourceId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to
    ) {
        return bookingService.getAvailability(resourceId, from, to);
    }

    @PostMapping("/{bookingId}/cancel")
    public BookingDto cancelMyBooking(@PathVariable UUID bookingId, HttpServletRequest request) {
        return bookingService.cancelMyBooking(bookingId, request);
    }

}
