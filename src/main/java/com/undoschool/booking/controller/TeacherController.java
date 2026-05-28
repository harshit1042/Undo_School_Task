package com.undoschool.booking.controller;

import com.undoschool.booking.dto.AddSessionRequest;
import com.undoschool.booking.dto.CreateOfferingRequest;
import com.undoschool.booking.dto.OfferingResponse;
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
@RequestMapping("/api/teacher")
@RequiredArgsConstructor
public class TeacherController {

    private final OfferingService offeringService;

    @PostMapping("/offerings")
    public OfferingResponse createOffering(@Valid @RequestBody CreateOfferingRequest request) {
        return offeringService.createOffering(request);
    }

    @PostMapping("/offerings/{offeringId}/sessions")
    public OfferingResponse addSessions(
            @PathVariable UUID offeringId,
            @Valid @RequestBody List<AddSessionRequest> sessions
    ) {
        return offeringService.addSessions(offeringId, sessions);
    }

    @GetMapping("/{teacherId}/offerings")
    public List<OfferingResponse> getTeacherOfferings(
            @PathVariable UUID teacherId,
            @RequestParam(defaultValue = "UTC") String timezone
    ) {
        return offeringService.getTeacherOfferings(teacherId, timezone);
    }
}
