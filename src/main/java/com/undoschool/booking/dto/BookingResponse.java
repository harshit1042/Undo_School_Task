package com.undoschool.booking.dto;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

public record BookingResponse(
        UUID bookingId,
        UUID parentId,
        UUID offeringId,
        String courseTitle,
        String offeringName,
        ZonedDateTime bookedAt,
        List<SessionResponse> sessions
) {
}
