package com.sahil.billing.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class BillItemForm {

    @DateTimeFormat(pattern = "dd-MM-yyyy")
    private LocalDate itemDate;

    private String buyerName;      // Particulars column
    private String materialType;   // Type column
    private Integer qty;
    private BigDecimal rate;

    /**
     * Date, type and rate are pre-filled automatically, so they must not
     * make a row count as "filled". A row is unused (and skipped on save)
     * when BOTH buyer and qty are empty.
     */
    public boolean isEmpty() {
        return (buyerName == null || buyerName.isBlank()) && qty == null;
    }

    /**
     * For a row that is NOT empty: describes what is wrong with it
     * (e.g. "please enter Rate"), or returns null when the row is complete.
     * An empty row always returns null (it is simply skipped).
     */
    public String problem() {
        if (isEmpty()) return null;

        List<String> missing = new ArrayList<>();
        if (itemDate == null)                                  missing.add("Date");
        if (buyerName == null || buyerName.isBlank())          missing.add("Buyer");
        if (materialType == null || materialType.isBlank())    missing.add("Type");
        if (qty == null)                                       missing.add("Qty");
        if (rate == null)                                      missing.add("Rate");

        List<String> parts = new ArrayList<>();
        if (!missing.isEmpty())                parts.add("please enter " + String.join(", ", missing));
        if (qty != null && qty < 1)            parts.add("Qty must be at least 1");
        if (rate != null && rate.signum() < 0) parts.add("Rate cannot be negative");

        return parts.isEmpty() ? null : String.join("; ", parts);
    }
}
