package com.sahil.billing;

import com.sahil.billing.dto.BillItemForm;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/** Row rules: empty rows are skipped, half-filled rows are reported, complete rows pass. */
class BillItemFormTest {

    private BillItemForm complete() {
        BillItemForm f = new BillItemForm();
        f.setItemDate(LocalDate.of(2026, 10, 1));
        f.setBuyerName("Arham Trading");
        f.setMaterialType("Wheat");
        f.setQty(100);
        f.setRate(new BigDecimal("5.00"));
        return f;
    }

    @Test
    void rowWithOnlyAutoFilledValuesIsEmptyAndNotReported() {
        BillItemForm f = new BillItemForm();          // date / type / rate pre-filled, nothing typed
        f.setItemDate(LocalDate.of(2026, 10, 1));
        f.setMaterialType("Wheat");
        f.setRate(new BigDecimal("5"));
        assertTrue(f.isEmpty());
        assertNull(f.problem());
    }

    @Test
    void completeRowHasNoProblem() {
        assertFalse(complete().isEmpty());
        assertNull(complete().problem());
    }

    @Test
    void zeroRateIsAllowed() {
        BillItemForm f = complete();
        f.setRate(BigDecimal.ZERO);
        assertNull(f.problem());
    }

    @Test
    void buyerWithoutQtyIsReported() {
        BillItemForm f = complete();
        f.setQty(null);
        assertEquals("please enter Qty", f.problem());
    }

    @Test
    void qtyWithoutBuyerIsReported() {
        BillItemForm f = complete();
        f.setBuyerName("  ");
        assertEquals("please enter Buyer", f.problem());
    }

    @Test
    void missingRateIsReportedInsteadOfSavingZero() {
        BillItemForm f = complete();
        f.setRate(null);
        assertEquals("please enter Rate", f.problem());
    }

    @Test
    void severalMissingFieldsAreListedTogether() {
        BillItemForm f = complete();
        f.setItemDate(null);
        f.setMaterialType("");
        f.setRate(null);
        assertEquals("please enter Date, Type, Rate", f.problem());
    }

    @Test
    void invalidNumbersAreReported() {
        BillItemForm f = complete();
        f.setQty(0);
        f.setRate(new BigDecimal("-1"));
        assertEquals("Qty must be at least 1; Rate cannot be negative", f.problem());
    }
}
