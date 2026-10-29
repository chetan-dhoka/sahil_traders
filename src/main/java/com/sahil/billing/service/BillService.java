package com.sahil.billing.service;

import com.sahil.billing.config.FirmContext;
import com.sahil.billing.dto.BillForm;
import com.sahil.billing.dto.BillItemForm;
import com.sahil.billing.entity.*;
import com.sahil.billing.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * All bill operations. The firm is always taken from FirmContext (the firm the user
 * selected), never from the caller, so one firm can never read or change another
 * firm's bills.
 */
@Service
@Transactional
public class BillService {

    @Autowired private BillRepository         billRepo;
    @Autowired private BillSequenceRepository seqRepo;
    @Autowired private SellerRepository       sellerRepo;
    @Autowired private BuyerRepository        buyerRepo;

    // ── Next bill number (per firm, pessimistic lock) ────────
    public Long nextBillNo() {
        Long firmId = FirmContext.requireFirmId();
        BillSequence seq = seqRepo.findForUpdate(firmId).orElse(null);
        if (seq == null) {                       // safety net for a brand-new firm
            seq = new BillSequence();
            seq.setFirmId(firmId);
            seq.setLastNo(0L);
            seq = seqRepo.save(seq);
        }
        long next = seq.getLastNo() + 1;
        seq.setLastNo(next);
        seqRepo.save(seq);
        return next;
    }

    // ── Create ───────────────────────────────────────────────
    public Bill create(BillForm form) {
        Long   firmId = FirmContext.requireFirmId();
        Seller seller = resolveOrCreateSeller(form.getSellerName());
        Long   billNo = nextBillNo();

        Bill bill = new Bill();
        bill.setFirmId(firmId);
        bill.setBillNo(billNo);
        bill.setBillDate(form.getBillDate());
        bill.setSeller(seller);

        List<BillItem> items = buildItems(form.getItems(), bill);
        bill.setItems(items);
        bill.recalculateTotal();

        return billRepo.save(bill);
    }

    // ── Update ───────────────────────────────────────────────
    public Bill update(Long id, BillForm form) {
        Bill bill = billRepo.findByIdWithDetails(id, FirmContext.requireFirmId())
                .orElseThrow(() -> new RuntimeException("Bill not found: " + id));

        Seller seller = resolveOrCreateSeller(form.getSellerName());
        bill.setBillDate(form.getBillDate());
        bill.setSeller(seller);
        bill.setUpdatedAt(LocalDateTime.now());

        bill.getItems().clear();
        bill.getItems().addAll(buildItems(form.getItems(), bill));
        bill.recalculateTotal();

        return billRepo.save(bill);
    }

    // ── Read ─────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public Optional<Bill> findById(Long id) {
        return billRepo.findByIdWithDetails(id, FirmContext.requireFirmId());
    }

    @Transactional(readOnly = true)
    public Optional<Bill> findByBillNo(Long billNo) {
        return billRepo.findByFirmIdAndBillNo(FirmContext.requireFirmId(), billNo);
    }

    @Transactional(readOnly = true)
    public Page<Bill> search(Long billNo, String sellerName,
                             LocalDate from, LocalDate to, int page) {
        return billRepo.search(FirmContext.requireFirmId(), billNo, sellerName, from, to,
                               PageRequest.of(page, 20));
    }

    /** Latest bills of the selected firm (home screen). */
    @Transactional(readOnly = true)
    public List<Bill> recent(int count) {
        return billRepo.search(FirmContext.requireFirmId(), null, null, null, null,
                               PageRequest.of(0, count)).getContent();
    }

    @Transactional(readOnly = true)
    public List<Bill> findByDateRange(LocalDate from, LocalDate to) {
        return billRepo.findByDateRange(FirmContext.requireFirmId(), from, to);
    }

    @Transactional(readOnly = true)
    public List<Bill> findBySellerAndDateRange(Long sellerId,
                                               LocalDate from, LocalDate to) {
        return billRepo.findBySellerAndDateRange(FirmContext.requireFirmId(), sellerId, from, to);
    }

    // ── Delete ───────────────────────────────────────────────
    public void delete(Long id) {
        Bill bill = billRepo.findByIdWithDetails(id, FirmContext.requireFirmId())
                .orElseThrow(() -> new RuntimeException("Bill not found: " + id));
        billRepo.delete(bill);
    }

    // ── Helpers (sellers and buyers are shared by all firms) ─
    private Seller resolveOrCreateSeller(String name) {
        String lower = name.trim().toLowerCase();
        return sellerRepo.findByNameLower(lower)
                .orElseGet(() -> sellerRepo.save(new Seller(name)));
    }

    private Buyer resolveOrCreateBuyer(String name) {
        String lower = name.trim().toLowerCase();
        return buyerRepo.findByNameLower(lower)
                .orElseGet(() -> buyerRepo.save(new Buyer(name)));
    }

    private List<BillItem> buildItems(List<BillItemForm> forms, Bill bill) {
        List<BillItem> items = new ArrayList<>();
        int order = 1;
        for (BillItemForm f : forms) {
            if (f.isEmpty()) continue;
            Buyer buyer = resolveOrCreateBuyer(f.getBuyerName());
            BillItem item = new BillItem();
            item.setBill(bill);
            item.setItemDate(f.getItemDate());
            item.setBuyer(buyer);
            item.setMaterialType(f.getMaterialType());
            item.setQty(f.getQty());
            item.setRate(f.getRate());
            item.setSortOrder(order++);
            item.computeAmount();
            items.add(item);
        }
        return items;
    }
}
