package com.it342.g3.backend.repository;

import com.it342.g3.backend.model.Pen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PenRepository extends JpaRepository<Pen, Long> {
    Optional<Pen> findByPenIdentifier(String penIdentifier);
    List<Pen> findByUserId(Long userId);
    boolean existsByPenIdentifier(String penIdentifier);
}
