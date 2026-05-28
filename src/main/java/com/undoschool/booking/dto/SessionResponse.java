package com.undoschool.booking.dto;

import java.time.ZonedDateTime;
import java.util.UUID;

public record SessionResponse(
        UUID sessionId,
        ZonedDateTime startAt,
        ZonedDateTime endAt
) {
}
