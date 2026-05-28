package com.undoschool.booking.repository;

import com.undoschool.booking.entity.Offering;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OfferingRepository extends JpaRepository<Offering, UUID> {
    List<Offering> findByTeacherId(UUID teacherId);
}
