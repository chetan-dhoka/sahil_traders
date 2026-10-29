package com.sahil.billing;

import com.sahil.billing.entity.*;
import com.sahil.billing.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Proves that bills of one firm are never visible through another firm's queries.
 * Runs on an in-memory H2 database (Flyway is switched off; tables come from the entities).
 */
@DataJpaTest(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"
})
class FirmIsolationTest {

    @Autowired private BillRepository         billRepo;
    @Autowired private BillSequenceRepository seqRepo;
    @Autowired private SellerRepository       sellerRepo;
    @Autowired private BuyerRepository        buyerRepo;
    @Autowired private FirmSettingsRepository firmRepo;
    @Autowired private TestEntityManager      em;

    private Long   firmA;
    private Long   firmB;
    private Bill   billA;   // firm A, bill no 1
    private Bill   billB;   // firm B, bill no 1  (same number on purpose)

    @BeforeEach
    void setUp() {
        firmA = newFirm("Firm A");
        firmB = newFirm("Firm B");

        Seller seller = sellerRepo.save(new Seller("Ravi Kumar"));   // shared by both firms
        Buyer  buyer  = buyerRepo.save(new Buyer("Arham Trading")); // shared by both firms

        billA = newBill(firmA, 1L, LocalDate.of(2026, 4, 10), seller, buyer, "100.00");
        billB = newBill(firmB, 1L, LocalDate.of(2026, 4, 10), seller, buyer, "999.00");

        em.flush();
        em.clear();
    }

    @Test
    void dateRangeReturnsOnlyOwnFirmsBills() {
        List<Bill> a = billRepo.findByDateRange(firmA,
                LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 30));
        assertEquals(1, a.size());
        assertEquals(firmA, a.get(0).getFirmId());
        assertEquals(0, new BigDecimal("100.00").compareTo(a.get(0).getTotalAmount()));
    }

    @Test
    void billOfAnotherFirmCannotBeOpenedById() {
        assertTrue(billRepo.findByIdWithDetails(billB.getId(), firmA).isEmpty());
        assertTrue(billRepo.findByIdWithDetails(billA.getId(), firmB).isEmpty());
        assertTrue(billRepo.findByIdWithDetails(billA.getId(), firmA).isPresent());
    }

    @Test
    void searchReturnsOnlyOwnFirmsBills() {
        Page<Bill> page = billRepo.search(firmB, 1L, "ravi",
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), PageRequest.of(0, 20));
        assertEquals(1, page.getTotalElements());
        assertEquals(firmB, page.getContent().get(0).getFirmId());
    }

    @Test
    void sellerFilterIsAlsoLimitedToTheFirm() {
        Long sellerId = sellerRepo.findByNameLower("ravi kumar").orElseThrow().getId();
        List<Bill> a = billRepo.findBySellerAndDateRange(firmA, sellerId,
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31));
        assertEquals(1, a.size());
        assertEquals(firmA, a.get(0).getFirmId());
    }

    @Test
    void eachFirmHasItsOwnBillNumberCounter() {
        newSequence(firmA, 5L);
        newSequence(firmB, 0L);
        em.flush();
        em.clear();

        assertEquals(5L, seqRepo.findForUpdate(firmA).orElseThrow().getLastNo());
        assertEquals(0L, seqRepo.findForUpdate(firmB).orElseThrow().getLastNo());
    }

    // ── helpers ──────────────────────────────────────────────
    private Long newFirm(String name) {
        FirmSettings f = new FirmSettings();
        f.setFirmName(name);
        f.setTheme("MAROON");
        return firmRepo.save(f).getId();
    }

    private void newSequence(Long firmId, long last) {
        BillSequence s = new BillSequence();
        s.setFirmId(firmId);
        s.setLastNo(last);
        seqRepo.save(s);
    }

    private Bill newBill(Long firmId, Long no, LocalDate date,
                         Seller seller, Buyer buyer, String amount) {
        Bill b = new Bill();
        b.setFirmId(firmId);
        b.setBillNo(no);
        b.setBillDate(date);
        b.setSeller(seller);

        BillItem it = new BillItem();
        it.setBill(b);
        it.setItemDate(date);
        it.setBuyer(buyer);
        it.setMaterialType("Wheat");
        it.setQty(1);
        it.setRate(new BigDecimal(amount));
        it.setSortOrder(1);
        it.computeAmount();

        b.getItems().add(it);
        b.recalculateTotal();
        return billRepo.save(b);
    }
}
