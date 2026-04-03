package com.it342.g3.backend.repository;

import com.it342.g3.backend.model.Feeding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeedingRepository extends JpaRepository<Feeding, Long> {
    List<Feeding> findByPenPenId(Long penId);
    List<Feeding> findByPenPenIdIn(List<Long> penIds);
    List<Feeding> findByRecordedById(Long userId);
}
