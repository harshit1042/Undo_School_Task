package com.undoschool.booking.service;

import com.undoschool.booking.dto.AddSessionRequest;
import com.undoschool.booking.dto.CreateOfferingRequest;
import com.undoschool.booking.dto.OfferingResponse;
import com.undoschool.booking.entity.Course;
import com.undoschool.booking.entity.Offering;
import com.undoschool.booking.entity.Session;
import com.undoschool.booking.exception.BadRequestException;
import com.undoschool.booking.exception.NotFoundException;
import com.undoschool.booking.repository.CourseRepository;
import com.undoschool.booking.repository.OfferingRepository;
import com.undoschool.booking.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OfferingService {

    private final CourseRepository courseRepository;
    private final OfferingRepository offeringRepository;
    private final SessionRepository sessionRepository;
    private final TimezoneMapper timezoneMapper;

    @Transactional
    public OfferingResponse createOffering(CreateOfferingRequest request) {
        ZoneId zoneId = timezoneMapper.parseZoneId(request.teacherTimezone());
        Course course = courseRepository.findByTitleIgnoreCase(request.courseTitle())
                .orElseGet(() -> {
                    Course c = new Course();
                    c.setTitle(request.courseTitle());
                    return courseRepository.save(c);
                });

        Offering offering = new Offering();
        offering.setCourse(course);
        offering.setTeacherId(request.teacherId());
        offering.setName(request.offeringName());
        offering.setTimezone(zoneId.getId());
        Offering saved = offeringRepository.save(offering);

        return timezoneMapper.toOfferingResponse(saved, List.of(), zoneId);
    }

    @Transactional
    public OfferingResponse addSessions(UUID offeringId, List<AddSessionRequest> requestSessions) {
        Offering offering = offeringRepository.findById(offeringId)
                .orElseThrow(() -> new NotFoundException("Offering not found: " + offeringId));

        ZoneId zoneId = timezoneMapper.parseZoneId(offering.getTimezone());
        List<Session> existing = sessionRepository.findByOfferingIdOrderByStartAtUtcAsc(offeringId);

        List<Session> newSessions = requestSessions.stream().map(req -> {
            if (!req.endAtLocal().isAfter(req.startAtLocal())) {
                throw new BadRequestException("Session end must be after start");
            }

            Session s = new Session();
            s.setOffering(offering);
            s.setStartAtUtc(req.startAtLocal().atZone(zoneId).toInstant());
            s.setEndAtUtc(req.endAtLocal().atZone(zoneId).toInstant());
            return s;
        }).toList();

        existing.addAll(newSessions);
        existing.sort(Comparator.comparing(Session::getStartAtUtc));
        for (int i = 1; i < existing.size(); i++) {
            if (existing.get(i).getStartAtUtc().isBefore(existing.get(i - 1).getEndAtUtc())) {
                throw new BadRequestException("Sessions in same offering must not overlap");
            }
        }

        List<Session> savedNewSessions = sessionRepository.saveAll(newSessions);
        List<Session> allSaved = sessionRepository.findByOfferingIdOrderByStartAtUtcAsc(offeringId);
        return timezoneMapper.toOfferingResponse(offering, allSaved, zoneId);
    }

    @Transactional(readOnly = true)
    public List<OfferingResponse> getTeacherOfferings(UUID teacherId, String viewTimezone) {
        ZoneId zoneId = timezoneMapper.parseZoneId(viewTimezone);
        return offeringRepository.findByTeacherId(teacherId).stream()
                .map(offering -> timezoneMapper.toOfferingResponse(
                        offering,
                        sessionRepository.findByOfferingIdOrderByStartAtUtcAsc(offering.getId()),
                        zoneId
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<OfferingResponse> getAllOfferings(String viewTimezone) {
        ZoneId zoneId = timezoneMapper.parseZoneId(viewTimezone);
        return offeringRepository.findAll().stream()
                .map(offering -> timezoneMapper.toOfferingResponse(
                        offering,
                        sessionRepository.findByOfferingIdOrderByStartAtUtcAsc(offering.getId()),
                        zoneId
                ))
                .toList();
    }
}
