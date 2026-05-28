package com.undoschool.booking.dto;

import java.util.List;
import java.util.UUID;

public record OfferingResponse(
        UUID offeringId,
        String courseTitle,
        String offeringName,
        UUID teacherId,
        String teacherTimezone,
        List<SessionResponse> sessions
) {
}
