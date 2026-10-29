package com.sahil.billing;

import com.sahil.billing.config.FirmContext;
import com.sahil.billing.config.Theme;
import com.sahil.billing.entity.FirmSettings;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ThemeAndFirmContextTest {

    @Test
    void unknownThemeFallsBackToMaroon() {
        assertSame(Theme.MAROON, Theme.of(null));
        assertSame(Theme.MAROON, Theme.of("nonsense"));
    }

    @Test
    void themeLookupIsCaseInsensitive() {
        assertSame(Theme.NAVY, Theme.of("navy"));
        assertSame(Theme.NAVY, Theme.of(" NAVY "));
        assertTrue(Theme.isKnown("plum"));
        assertFalse(Theme.isKnown("green"));
    }

    @Test
    void originalSahilColoursAreUnchanged() {
        assertEquals("#8B1A1A", Theme.MAROON.getPrimary());
        assertEquals("#762b2b", Theme.MAROON.getPdfPrimary());
    }

    @Test
    void firmContextRequiresASelectedFirm() {
        FirmContext.clear();
        assertThrows(IllegalStateException.class, FirmContext::requireFirmId);

        FirmSettings f = new FirmSettings();
        f.setId(2L);
        FirmContext.set(f);
        assertEquals(2L, FirmContext.requireFirmId());

        FirmContext.clear();
        assertNull(FirmContext.get());
    }
}
