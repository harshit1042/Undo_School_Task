package com.undoschool.booking.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record AddSessionRequest(
        @NotNull LocalDateTime startAtLocal,
        @NotNull LocalDateTime endAtLocal
) {
}
