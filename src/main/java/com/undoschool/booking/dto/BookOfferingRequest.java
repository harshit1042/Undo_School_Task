package com.undoschool.booking.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record BookOfferingRequest(
        @NotNull UUID parentId,
        @NotNull UUID offeringId
) {
}
