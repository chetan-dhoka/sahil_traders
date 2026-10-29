package com.sahil.billing.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "bill_items")
@Data
@NoArgsConstructor
@ToString(exclude = "bill")
public class BillItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bill_id", nullable = false)
    private Bill bill;

    @Column(name = "item_date", nullable = false)
    private LocalDate itemDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_id", nullable = false)
    private Buyer buyer;

    @Column(name = "material_type", nullable = false, length = 100)
    private String materialType;

    @Column(nullable = false)
    private Integer qty;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal rate;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    public void computeAmount() {
        if (qty != null && rate != null) {
            this.amount = rate.multiply(BigDecimal.valueOf(qty));
        } else {
            this.amount = BigDecimal.ZERO; // safety fallback
        }
    }
}
