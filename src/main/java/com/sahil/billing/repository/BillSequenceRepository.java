package com.sahil.billing.repository;

import com.sahil.billing.entity.BillSequence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.Optional;

@Repository
public interface BillSequenceRepository extends JpaRepository<BillSequence, Long> {

    /** Locks the counter row of ONE firm while a new bill number is handed out. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM BillSequence s WHERE s.firmId = :firmId")
    Optional<BillSequence> findForUpdate(@Param("firmId") Long firmId);
}
