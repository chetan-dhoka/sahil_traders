package com.sahil.billing.repository;

import com.sahil.billing.entity.Buyer;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BuyerRepository extends JpaRepository<Buyer, Long> {

    Optional<Buyer> findByNameLower(String nameLower);

    @Query("SELECT b FROM Buyer b WHERE b.active = true " +
           "AND b.nameLower LIKE CONCAT(:q, '%') ORDER BY b.name")
    List<Buyer> suggest(@Param("q") String q, Pageable pageable);

    @Query("SELECT b FROM Buyer b WHERE b.active = true ORDER BY b.name")
    List<Buyer> findAllActive();

    boolean existsByNameLowerAndIdNot(String nameLower, Long id);
}
