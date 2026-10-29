package com.sahil.billing.config;

import lombok.Builder;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

/**
 * Colour palettes. Each firm picks one by key (stored in firm_settings.theme).
 * MAROON reproduces the original Sahil Traders colours exactly.
 * To add another colour later, add one more constant below.
 */
@Getter
@Builder
public class Theme {

    private final String key;
    private final String label;

    // ── Web screens (CSS variables) ──
    private final String primary;      // --red
    private final String hover;        // --red2
    private final String cream;        // page background
    private final String border;
    private final String tint;         // table header / hover
    private final String tint2;
    private final String tint3;
    private final String line;
    private final String line2;
    private final String labelColor;
    private final String rgb;          // "r,g,b" of primary, used for shadows

    // ── Printed bill (PDF) ──
    private final String pdfPrimary;
    private final String pdfHeadLine;  // column-heading divider
    private final String pdfTint;      // total rows / top strip
    private final String pdfLine;      // outer border
    private final String pdfLine2;
    private final String pdfLine3;
    private final String pdfRowLine;
    private final String pdfStripe;    // alternate row
    private final String pdfPaper;     // meta / footer background

    public static final Theme MAROON = Theme.builder()
            .key("MAROON").label("Maroon")
            .primary("#8B1A1A").hover("#C0392B").cream("#FDF6F0").border("#d4a0a0")
            .tint("#fbeaea").tint2("#fff8f8").tint3("#fdf0f0")
            .line("#edd5d5").line2("#f5e0e0").labelColor("#5a2020").rgb("139,26,26")
            .pdfPrimary("#762b2b").pdfHeadLine("#925858").pdfTint("#f6f0e9")
            .pdfLine("#d9c8bd").pdfLine2("#e4d8d0").pdfLine3("#eee6e0")
            .pdfRowLine("#e8dfd9").pdfStripe("#fbf8f5").pdfPaper("#fffdfb")
            .build();

    public static final Theme NAVY = Theme.builder()
            .key("NAVY").label("Navy Blue")
            .primary("#1F3A5F").hover("#2F5D9B").cream("#F3F6FB").border("#a9bcd6")
            .tint("#e6edf7").tint2("#f6f9fd").tint3("#eef3fa")
            .line("#d3deee").line2("#e3ebf6").labelColor("#14284a").rgb("31,58,95")
            .pdfPrimary("#1F3A5F").pdfHeadLine("#4a6a94").pdfTint("#eaf0f7")
            .pdfLine("#c5d3e6").pdfLine2("#d6e0ee").pdfLine3("#e3eaf4")
            .pdfRowLine("#dbe3ee").pdfStripe("#f6f9fd").pdfPaper("#fbfdff")
            .build();

    public static final Theme PLUM = Theme.builder()
            .key("PLUM").label("Plum")
            .primary("#5B2A6B").hover("#8E44AD").cream("#FAF5FC").border("#cdb0d8")
            .tint("#f1e4f6").tint2("#fcf8fd").tint3("#f7eef9")
            .line("#e4d1ec").line2("#efe3f4").labelColor("#3d1a49").rgb("91,42,107")
            .pdfPrimary("#5B2A6B").pdfHeadLine("#85579a").pdfTint("#f2e9f6")
            .pdfLine("#d9c5e2").pdfLine2("#e4d6ec").pdfLine3("#ede3f3")
            .pdfRowLine("#e6dbee").pdfStripe("#faf6fc").pdfPaper("#fdfbfe")
            .build();

    private static final List<Theme> ALL = Arrays.asList(MAROON, NAVY, PLUM);

    public static List<Theme> all() {
        return ALL;
    }

    /** Unknown / empty key falls back to MAROON (original look). */
    public static Theme of(String key) {
        if (key != null) {
            for (Theme t : ALL) {
                if (t.key.equalsIgnoreCase(key.trim())) return t;
            }
        }
        return MAROON;
    }

    public static boolean isKnown(String key) {
        if (key == null) return false;
        for (Theme t : ALL) {
            if (t.key.equalsIgnoreCase(key.trim())) return true;
        }
        return false;
    }
}
