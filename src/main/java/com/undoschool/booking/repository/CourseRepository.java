package com.undoschool.booking.repository;

import com.undoschool.booking.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CourseRepository extends JpaRepository<Course, UUID> {
    Optional<Course> findByTitleIgnoreCase(String title);
}
