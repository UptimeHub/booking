package uz.uptimehub.booking.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uz.uptimehub.booking.dto.booking.BookingCreateRequest;
import uz.uptimehub.booking.dto.booking.BookingDto;
import uz.uptimehub.booking.service.BookingService;

@RestController
@RequestMapping("/api/booking")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public BookingDto create(@Valid @RequestBody BookingCreateRequest body, HttpServletRequest request) {
        return bookingService.createBooking(body, request);
    }

}
