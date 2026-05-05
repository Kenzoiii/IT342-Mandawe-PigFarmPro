package com.it342.g3.backend.salesManagement.repository;

import com.it342.g3.backend.salesManagement.model.Sale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SaleRepository extends JpaRepository<Sale, Long> {
    Optional<Sale> findByPigPigId(Long pigId);
    List<Sale> findByPigPigIdIn(List<Long> pigIds);
}
