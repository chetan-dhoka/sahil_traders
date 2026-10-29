package com.sahil.billing.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "bills")
@Data
@NoArgsConstructor
public class Bill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The firm this bill belongs to. Every query filters on this. */
    @Column(name = "firm_id", nullable = false, updatable = false)
    private Long firmId;

    @Column(name = "bill_no", nullable = false)
    private Long billNo;

    @Column(name = "bill_date", nullable = false)
    private LocalDate billDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private Seller seller;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @OneToMany(mappedBy = "bill", cascade = CascadeType.ALL,
               orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("sortOrder ASC")
    private List<BillItem> items = new ArrayList<>();

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    public void recalculateTotal() {
        this.totalAmount = items.stream()
                .map(BillItem::getAmount)
                .filter(a -> a != null)          // skip any null amounts
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
