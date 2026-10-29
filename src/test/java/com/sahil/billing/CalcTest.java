package com.sahil.billing;

import com.sahil.billing.entity.Bill;
import com.sahil.billing.entity.BillItem;
import com.sahil.billing.util.DateRangeUtil;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class CalcTest {

    // ── BillItem amount calculation ───────────────────────────

    @Test
    void testAmountCalculation() {
        BillItem item = new BillItem();
        item.setQty(200);
        item.setRate(new BigDecimal("2.50"));
        item.computeAmount();
        assertEquals(new BigDecimal("500.00"), item.getAmount());
    }

    @Test
    void testZeroRate() {
        BillItem item = new BillItem();
        item.setQty(500);
        item.setRate(BigDecimal.ZERO);
        item.computeAmount();
        assertEquals(BigDecimal.ZERO, item.getAmount());
    }

    @Test
    void testBillTotal() {
        Bill bill = new Bill();
        BillItem i1 = item(100, "2.50");  // 250
        BillItem i2 = item(200, "1.75");  // 350
        BillItem i3 = item(50,  "4.00");  // 200
        i1.setBill(bill); i2.setBill(bill); i3.setBill(bill);
        bill.setItems(Arrays.asList(i1, i2, i3));
        bill.recalculateTotal();
        assertEquals(new BigDecimal("800.00"), bill.getTotalAmount());
    }

    // ── DateRangeUtil ─────────────────────────────────────────

    @Test void fyYearApril()  { assertEquals(2024, DateRangeUtil.fyYear(LocalDate.of(2024,4,1))); }
    @Test void fyYearMarch()  { assertEquals(2023, DateRangeUtil.fyYear(LocalDate.of(2024,3,31))); }

    @Test void q1Range() {
        var r = DateRangeUtil.q1(2024);
        assertEquals(LocalDate.of(2024,4,1),  r.from);
        assertEquals(LocalDate.of(2024,6,30), r.to);
    }
    @Test void q2Range() {
        var r = DateRangeUtil.q2(2024);
        assertEquals(LocalDate.of(2024,7,1),  r.from);
        assertEquals(LocalDate.of(2024,9,30), r.to);
    }
    @Test void q3Range() {
        var r = DateRangeUtil.q3(2024);
        assertEquals(LocalDate.of(2024,10,1),  r.from);
        assertEquals(LocalDate.of(2024,12,31), r.to);
    }
    @Test void q4Range() {
        var r = DateRangeUtil.q4(2024);
        assertEquals(LocalDate.of(2025,1,1),  r.from);
        assertEquals(LocalDate.of(2025,3,31), r.to);
    }
    @Test void h1Range() {
        var r = DateRangeUtil.h1(2024);
        assertEquals(LocalDate.of(2024,4,1),  r.from);
        assertEquals(LocalDate.of(2024,9,30), r.to);
    }
    @Test void h2Range() {
        var r = DateRangeUtil.h2(2024);
        assertEquals(LocalDate.of(2024,10,1), r.from);
        assertEquals(LocalDate.of(2025,3,31), r.to);
    }
    @Test void yearlyRange() {
        var r = DateRangeUtil.yearly(2024);
        assertEquals(LocalDate.of(2024,4,1),  r.from);
        assertEquals(LocalDate.of(2025,3,31), r.to);
        assertTrue(r.label.contains("2024"));
    }
    @Test void customRange() {
        LocalDate from = LocalDate.of(2024,6,1);
        LocalDate to   = LocalDate.of(2024,8,31);
        var r = DateRangeUtil.resolve("custom", null, from, to);
        assertEquals(from, r.from);
        assertEquals(to,   r.to);
    }

    // ── Helper ────────────────────────────────────────────────
    private BillItem item(int qty, String rate) {
        BillItem i = new BillItem();
        i.setQty(qty);
        i.setRate(new BigDecimal(rate));
        i.computeAmount();
        return i;
    }
}
