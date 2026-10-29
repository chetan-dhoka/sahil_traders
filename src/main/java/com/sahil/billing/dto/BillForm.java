package com.sahil.billing.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
public class BillForm {

    private Long id;

    /** The firm this form was opened for. Checked on save so a form left open in an
     *  old tab can never be saved into a different firm. */
    private Long firmId;

    @NotNull(message = "Bill date is required")
    @DateTimeFormat(pattern = "dd-MM-yyyy")
    private LocalDate billDate;

    @NotBlank(message = "Seller name is required")
    private String sellerName;

    @Valid
    private List<BillItemForm> items = new ArrayList<>();

    public void addEmptyItem() {
        items.add(new BillItemForm());
    }
}
