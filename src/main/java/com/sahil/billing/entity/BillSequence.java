package com.sahil.billing.entity;

import lombok.Data;
import jakarta.persistence.*;

@Entity
@Table(name = "bill_sequence")
@Data
public class BillSequence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "firm_id", nullable = false, unique = true)
    private Long firmId;

    @Column(name = "last_no", nullable = false)
    private Long lastNo = 0L;
}
