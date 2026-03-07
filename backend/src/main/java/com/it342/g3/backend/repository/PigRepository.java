package com.it342.g3.backend.repository;

import com.it342.g3.backend.model.Pig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PigRepository extends JpaRepository<Pig, Long> {
    Optional<Pig> findByPigIdentifier(String pigIdentifier);
    List<Pig> findByPenPenId(Long penId);
    boolean existsByPigIdentifier(String pigIdentifier);
}
