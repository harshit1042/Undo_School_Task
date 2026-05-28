package com.undoschool.booking.controller;

import com.undoschool.booking.dto.BookOfferingRequest;
import com.undoschool.booking.dto.BookingResponse;
import com.undoschool.booking.dto.OfferingResponse;
import com.undoschool.booking.service.BookingService;
import com.undoschool.booking.service.OfferingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/parent")
@RequiredArgsConstructor
public class ParentController {

    private final OfferingService offeringService;
    private final BookingService bookingService;

    @GetMapping("/offerings")
    public List<OfferingResponse> getAvailableOfferings(
            @RequestParam(defaultValue = "UTC") String timezone
    ) {
        return offeringService.getAllOfferings(timezone);
    }

    @PostMapping("/bookings")
    public BookingResponse bookOffering(
            @Valid @RequestBody BookOfferingRequest request,
            @RequestParam(defaultValue = "UTC") String timezone
    ) {
        return bookingService.bookOffering(request.parentId(), request.offeringId(), timezone);
    }

    @GetMapping("/{parentId}/bookings")
    public List<BookingResponse> getBookings(
            @PathVariable UUID parentId,
            @RequestParam(defaultValue = "UTC") String timezone
    ) {
        return bookingService.getBookings(parentId, timezone);
    }
}
