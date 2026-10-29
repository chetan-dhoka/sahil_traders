package com.sahil.billing.pdf;

import com.sahil.billing.entity.BillItem;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

/** One printed page of a bill: up to 19 item rows + blank filler rows. */
@Getter
@AllArgsConstructor
public class BillPage {
    private int            pageNo;
    private int            totalPages;
    private List<BillItem> items;
    private BigDecimal     pageTotal;
    private List<Integer>  fillerRows;   // blank rows to reach 19
    private boolean        lastPage;
}
