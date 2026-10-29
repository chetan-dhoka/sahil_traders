package com.sahil.billing.config;

import com.sahil.billing.entity.FirmSettings;

/**
 * Holds the firm selected by the user for the request currently being handled.
 * Every service reads the firm from here, so no screen can forget the firm filter.
 */
public final class FirmContext {

    private static final ThreadLocal<FirmSettings> CURRENT = new ThreadLocal<>();

    private FirmContext() { }

    public static void set(FirmSettings firm) { CURRENT.set(firm); }

    public static FirmSettings get() { return CURRENT.get(); }

    public static void clear() { CURRENT.remove(); }

    public static FirmSettings requireFirm() {
        FirmSettings f = CURRENT.get();
        if (f == null) {
            throw new IllegalStateException("No firm selected");
        }
        return f;
    }

    public static Long requireFirmId() {
        return requireFirm().getId();
    }
}
