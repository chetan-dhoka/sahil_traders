package com.sahil.billing.repository;

import com.sahil.billing.entity.Seller;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SellerRepository extends JpaRepository<Seller, Long> {

    Optional<Seller> findByNameLower(String nameLower);

    @Query("SELECT s FROM Seller s WHERE s.active = true " +
           "AND s.nameLower LIKE CONCAT(:q, '%') ORDER BY s.name")
    List<Seller> suggest(@Param("q") String q, Pageable pageable);

    @Query("SELECT s FROM Seller s WHERE s.active = true ORDER BY s.name")
    List<Seller> findAllActive();

    boolean existsByNameLowerAndIdNot(String nameLower, Long id);
}
