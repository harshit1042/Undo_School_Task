package com.undoschool.booking.service;

import com.undoschool.booking.dto.OfferingResponse;
import com.undoschool.booking.dto.SessionResponse;
import com.undoschool.booking.entity.Offering;
import com.undoschool.booking.entity.Session;
import com.undoschool.booking.exception.BadRequestException;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.util.List;

@Component
public class TimezoneMapper {

    public ZoneId parseZoneId(String zoneText) {
        try {
            return ZoneId.of(zoneText);
        } catch (Exception ex) {
            throw new BadRequestException("Invalid timezone: " + zoneText);
        }
    }

    public OfferingResponse toOfferingResponse(Offering offering, List<Session> sessions, ZoneId viewerZone) {
        List<SessionResponse> sessionResponses = sessions.stream()
                .map(session -> new SessionResponse(
                        session.getId(),
                        session.getStartAtUtc().atZone(viewerZone),
                        session.getEndAtUtc().atZone(viewerZone)
                ))
                .toList();

        return new OfferingResponse(
                offering.getId(),
                offering.getCourse().getTitle(),
                offering.getName(),
                offering.getTeacherId(),
                offering.getTimezone(),
                sessionResponses
        );
    }
}
