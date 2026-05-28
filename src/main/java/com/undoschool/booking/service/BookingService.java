package com.undoschool.booking.service;

import com.undoschool.booking.dto.BookingResponse;
import com.undoschool.booking.dto.SessionResponse;
import com.undoschool.booking.entity.Booking;
import com.undoschool.booking.entity.Offering;
import com.undoschool.booking.entity.ParentUser;
import com.undoschool.booking.entity.Session;
import com.undoschool.booking.exception.ConflictException;
import com.undoschool.booking.exception.NotFoundException;
import com.undoschool.booking.repository.BookingRepository;
import com.undoschool.booking.repository.OfferingRepository;
import com.undoschool.booking.repository.ParentRepository;
import com.undoschool.booking.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final OfferingRepository offeringRepository;
    private final SessionRepository sessionRepository;
    private final ParentRepository parentRepository;
    private final TimezoneMapper timezoneMapper;

    @Transactional
    public BookingResponse bookOffering(UUID parentExternalId, UUID offeringId, String viewTimezone) {
        ZoneId zoneId = timezoneMapper.parseZoneId(viewTimezone);
        ParentUser parent = getOrCreateParent(parentExternalId);
        ParentUser lockedParent = parentRepository.findByIdForUpdate(parent.getId())
                .orElseThrow(() -> new NotFoundException("Parent not found"));

        Offering offering = offeringRepository.findById(offeringId)
                .orElseThrow(() -> new NotFoundException("Offering not found: " + offeringId));

        bookingRepository.findByParentIdAndOfferingId(lockedParent.getId(), offeringId)
                .ifPresent(existing -> {
                    throw new ConflictException("Offering already booked by this parent");
                });

        List<Session> candidateSessions = sessionRepository.findByOfferingIdOrderByStartAtUtcAsc(offeringId);
        for (Session candidateSession : candidateSessions) {
            List<Session> conflicts = sessionRepository.findConflictingBookedSessions(
                    lockedParent.getId(),
                    candidateSession.getStartAtUtc(),
                    candidateSession.getEndAtUtc()
            );
            if (!conflicts.isEmpty()) {
                throw new ConflictException("Booking conflicts with an already booked offering session");
            }
        }

        Booking booking = new Booking();
        booking.setParent(lockedParent);
        booking.setOffering(offering);
        booking.setBookedAtUtc(Instant.now());

        try {
            Booking saved = bookingRepository.save(booking);
            return toBookingResponse(saved, candidateSessions, zoneId);
        } catch (DataIntegrityViolationException ex) {
            throw new ConflictException("Offering already booked by this parent");
        }
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getBookings(UUID parentExternalId, String viewTimezone) {
        ZoneId zoneId = timezoneMapper.parseZoneId(viewTimezone);
        return bookingRepository.findByParentExternalId(parentExternalId).stream()
                .map(booking -> {
                    List<Session> sessions = sessionRepository.findByOfferingIdOrderByStartAtUtcAsc(
                            booking.getOffering().getId());
                    return toBookingResponse(booking, sessions, zoneId);
                })
                .toList();
    }

    private ParentUser getOrCreateParent(UUID externalId) {
        return parentRepository.findByExternalId(externalId)
                .orElseGet(() -> {
                    ParentUser parent = new ParentUser();
                    parent.setExternalId(externalId);
                    try {
                        return parentRepository.save(parent);
                    } catch (DataIntegrityViolationException ex) {
                        return parentRepository.findByExternalId(externalId)
                                .orElseThrow(() -> new ConflictException("Could not establish parent identity"));
                    }
                });
    }

    private BookingResponse toBookingResponse(Booking booking, List<Session> sessions, ZoneId viewerZone) {
        List<SessionResponse> sessionResponses = sessions.stream().map(session -> new SessionResponse(
                session.getId(),
                session.getStartAtUtc().atZone(viewerZone),
                session.getEndAtUtc().atZone(viewerZone)
        )).toList();

        return new BookingResponse(
                booking.getId(),
                booking.getParent().getExternalId(),
                booking.getOffering().getId(),
                booking.getOffering().getCourse().getTitle(),
                booking.getOffering().getName(),
                booking.getBookedAtUtc().atZone(viewerZone),
                sessionResponses
        );
    }
}
