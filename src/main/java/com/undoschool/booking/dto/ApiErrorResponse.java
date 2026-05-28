package com.undoschool.booking.dto;

import java.time.Instant;

public record ApiErrorResponse(String message, Instant timestamp) {
}
