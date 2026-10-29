package com.sahil.billing.repository;

import com.sahil.billing.entity.Bill;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * NOTE: every query here takes a firmId. Do not add a query without it,
 * otherwise bills of one firm could show up in another firm.
 */
@Repository
public interface BillRepository extends JpaRepository<Bill, Long> {

    Optional<Bill> findByFirmIdAndBillNo(Long firmId, Long billNo);

    @Query("SELECT b FROM Bill b JOIN FETCH b.seller " +
           "WHERE b.firmId = :firmId " +
           "AND (:billNo IS NULL OR b.billNo = :billNo) " +
           "AND (:sellerName IS NULL OR b.seller.nameLower LIKE CONCAT('%',:sellerName,'%')) " +
           "AND (:fromDate IS NULL OR b.billDate >= :fromDate) " +
           "AND (:toDate   IS NULL OR b.billDate <= :toDate) " +
           "ORDER BY b.billDate DESC, b.billNo DESC")
    Page<Bill> search(@Param("firmId")     Long firmId,
                      @Param("billNo")     Long billNo,
                      @Param("sellerName") String sellerName,
                      @Param("fromDate")   LocalDate fromDate,
                      @Param("toDate")     LocalDate toDate,
                      Pageable pageable);

    @Query("SELECT b FROM Bill b JOIN FETCH b.seller " +
           "WHERE b.firmId = :firmId " +
           "AND b.billDate >= :fromDate AND b.billDate <= :toDate " +
           "ORDER BY b.billDate ASC, b.billNo ASC")
    List<Bill> findByDateRange(@Param("firmId")   Long firmId,
                               @Param("fromDate") LocalDate fromDate,
                               @Param("toDate")   LocalDate toDate);

    @Query("SELECT b FROM Bill b JOIN FETCH b.seller " +
           "WHERE b.firmId = :firmId AND b.seller.id = :sellerId " +
           "AND b.billDate >= :fromDate AND b.billDate <= :toDate " +
           "ORDER BY b.billDate ASC, b.billNo ASC")
    List<Bill> findBySellerAndDateRange(@Param("firmId")   Long firmId,
                                        @Param("sellerId") Long sellerId,
                                        @Param("fromDate") LocalDate fromDate,
                                        @Param("toDate")   LocalDate toDate);

    /** A bill id that belongs to another firm simply returns nothing. */
    @Query("SELECT b FROM Bill b " +
           "JOIN FETCH b.seller " +
           "LEFT JOIN FETCH b.items i " +
           "LEFT JOIN FETCH i.buyer " +
           "WHERE b.id = :id AND b.firmId = :firmId")
    Optional<Bill> findByIdWithDetails(@Param("id") Long id, @Param("firmId") Long firmId);
}
