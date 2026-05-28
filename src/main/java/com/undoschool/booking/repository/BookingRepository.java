package com.undoschool.booking.repository;

import com.undoschool.booking.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BookingRepository extends JpaRepository<Booking, UUID> {
    List<Booking> findByParentExternalId(UUID parentExternalId);

    Optional<Booking> findByParentIdAndOfferingId(UUID parentId, UUID offeringId);
}
