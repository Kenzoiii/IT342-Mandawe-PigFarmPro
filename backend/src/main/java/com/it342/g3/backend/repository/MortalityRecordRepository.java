package com.it342.g3.backend.repository;

import com.it342.g3.backend.model.MortalityRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MortalityRecordRepository extends JpaRepository<MortalityRecord, Long> {
    Optional<MortalityRecord> findByPigPigId(Long pigId);
    List<MortalityRecord> findByRecordedById(Long userId);
}
