package com.sahil.billing.util;

import java.time.LocalDate;
import java.time.Month;

/**
 * India Financial Year helpers.
 * FY runs April 1 → March 31.
 * Q1: Apr-Jun | Q2: Jul-Sep | Q3: Oct-Dec | Q4: Jan-Mar
 * H1: Apr-Sep | H2: Oct-Mar
 */
public class DateRangeUtil {

    public static class DateRange {
        public final LocalDate from;
        public final LocalDate to;
        public final String    label;

        public DateRange(LocalDate from, LocalDate to, String label) {
            this.from  = from;
            this.to    = to;
            this.label = label;
        }
    }

    /** FY year for a date: April 2024 → 2024, March 2024 → 2023 */
    public static int fyYear(LocalDate d) {
        return d.getMonthValue() >= 4 ? d.getYear() : d.getYear() - 1;
    }

    public static int currentFyYear() {
        return fyYear(LocalDate.now());
    }

    public static DateRange yearly(int fy) {
        return new DateRange(
            LocalDate.of(fy,     Month.APRIL, 1),
            LocalDate.of(fy + 1, Month.MARCH, 31),
            "FY " + fy + "-" + String.valueOf(fy + 1).substring(2)
        );
    }

    public static DateRange q1(int fy) {
        return new DateRange(LocalDate.of(fy, Month.APRIL,  1),
                             LocalDate.of(fy, Month.JUNE,  30),
                             "Q1 Apr–Jun " + fy);
    }
    public static DateRange q2(int fy) {
        return new DateRange(LocalDate.of(fy, Month.JULY,      1),
                             LocalDate.of(fy, Month.SEPTEMBER, 30),
                             "Q2 Jul–Sep " + fy);
    }
    public static DateRange q3(int fy) {
        return new DateRange(LocalDate.of(fy, Month.OCTOBER,  1),
                             LocalDate.of(fy, Month.DECEMBER, 31),
                             "Q3 Oct–Dec " + fy);
    }
    public static DateRange q4(int fy) {
        return new DateRange(LocalDate.of(fy + 1, Month.JANUARY, 1),
                             LocalDate.of(fy + 1, Month.MARCH,  31),
                             "Q4 Jan–Mar " + (fy + 1));
    }
    public static DateRange h1(int fy) {
        return new DateRange(LocalDate.of(fy, Month.APRIL,     1),
                             LocalDate.of(fy, Month.SEPTEMBER, 30),
                             "H1 Apr–Sep " + fy);
    }
    public static DateRange h2(int fy) {
        return new DateRange(LocalDate.of(fy,     Month.OCTOBER, 1),
                             LocalDate.of(fy + 1, Month.MARCH,  31),
                             "H2 Oct–Mar " + fy + "/" + (fy + 1));
    }

    public static DateRange resolve(String period, Integer fy,
                                    LocalDate customFrom, LocalDate customTo) {
        int year = (fy != null) ? fy : currentFyYear();
        if (period == null) return yearly(year);
        switch (period.toLowerCase()) {
            case "q1":      return q1(year);
            case "q2":      return q2(year);
            case "q3":      return q3(year);
            case "q4":      return q4(year);
            case "h1":      return h1(year);
            case "h2":      return h2(year);
            case "custom":
                if (customFrom != null && customTo != null)
                    return new DateRange(customFrom, customTo, "Custom Range");
            default:        return yearly(year);
        }
    }
}
