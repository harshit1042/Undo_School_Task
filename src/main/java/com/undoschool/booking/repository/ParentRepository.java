package com.undoschool.booking.repository;

import com.undoschool.booking.entity.ParentUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;

public interface ParentRepository extends JpaRepository<ParentUser, UUID> {
    Optional<ParentUser> findByExternalId(UUID externalId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from ParentUser p where p.id = :id")
    Optional<ParentUser> findByIdForUpdate(@Param("id") UUID id);
}
