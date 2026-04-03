package com.it342.g3.backend.repository;

import com.it342.g3.backend.model.HealthRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HealthRecordRepository extends JpaRepository<HealthRecord, Long> {
    List<HealthRecord> findByPigPigId(Long pigId);
    List<HealthRecord> findByPigPigIdIn(List<Long> pigIds);
    List<HealthRecord> findByRecordedById(Long userId);
}
