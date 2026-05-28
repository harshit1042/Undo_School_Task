package com.undoschool.booking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateOfferingRequest(
        @NotBlank String courseTitle,
        @NotBlank String offeringName,
        @NotNull UUID teacherId,
        @NotBlank String teacherTimezone
) {
}
