package com.sahil.billing.entity;

import lombok.Data;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "firm_settings")
@Data
public class FirmSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "firm_name", nullable = false)
    private String firmName;

    private String subtitle;
    private String address;
    private String mobile;

    @Column(name = "pan_number")
    private String panNumber;

    @Column(name = "footer_line1")
    private String footerLine1;

    @Column(name = "footer_line2")
    private String footerLine2;

    /** Colour palette key: MAROON, NAVY, PLUM ... (see config/Theme) */
    @Column(name = "theme", nullable = false)
    private String theme = "MAROON";

    /** Initials shown in the round logo when there is no logo image. */
    @Column(name = "logo_text")
    private String logoText;

    /** Logo image under static/ (e.g. logos/ssd-logo.png). Empty = use initials circle. */
    @Column(name = "logo_path")
    private String logoPath;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
