package com.undoschool.booking.repository;

import com.undoschool.booking.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface SessionRepository extends JpaRepository<Session, UUID> {
    List<Session> findByOfferingIdOrderByStartAtUtcAsc(UUID offeringId);

    @Query("""
        select distinct s from Session s
        join Booking b on b.offering.id = s.offering.id
        where b.parent.id = :parentId
        and s.startAtUtc < :candidateEnd
        and s.endAtUtc > :candidateStart
        """)
    List<Session> findConflictingBookedSessions(
            @Param("parentId") UUID parentId,
            @Param("candidateStart") Instant candidateStart,
            @Param("candidateEnd") Instant candidateEnd
    );
}
