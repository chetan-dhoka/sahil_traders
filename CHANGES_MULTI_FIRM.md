# Multi-firm change list (compared with your previous project)

NEW files
- src/main/resources/db/migration/V2__multi_firm.sql   (adds firm_id, resets bills, adds S S Dhoka)
- src/main/resources/static/logos/ssd-logo.png
- src/main/resources/templates/select-firm.html
- config/Theme.java, FirmContext.java, FirmInterceptor.java, FirmModelAdvice.java
- controller/FirmController.java
- src/test/.../FirmIsolationTest.java, ThemeAndFirmContextTest.java
- extras/ (logo alternatives, optional cleanup SQL)

CHANGED files
- config/WebConfig.java            (interceptor registered)
- entity/FirmSettings.java         (theme, logoText, logoPath)
- entity/Bill.java                 (firmId; bill_no unique per firm only)
- entity/BillSequence.java         (one counter per firm)
- repository/BillRepository.java, BillSequenceRepository.java (every query takes firmId)
- service/BillService.java         (firm always taken from FirmContext)
- dto/BillForm.java                (firmId carried by the form)
- controller/HomeController.java, BillController.java, ReportController.java
- pdf/PdfService.java              (theme + logo passed to the PDF templates)
- templates: fragments/layout.html, home.html, settings.html, bill-form.html,
  bill-view.html, bill-list.html, reports.html, pdf/bill.html, pdf/report.html
- application.properties           (session timeout 8h)

UNCHANGED: V1__init.sql, bill-form.js, DateRangeUtil, SuggestController, entities Seller/Buyer/Bill items.

---------------------------------------------------------------------
BILL FORM IMPROVEMENTS (added after the multi-firm change)

1. Delete-row confirmation
   - Row with a buyer or qty  -> Bootstrap pop-up "Delete this row?" (Cancel is focused)
   - Row with neither         -> deleted straight away
   - Deleting the only row leaves one blank row
2. Automatic next row
   - Typing a Qty in the LAST row adds a row below (date / type / rate carried over)
   - Date / Type / Rate also follow the row above until a row gets a buyer or qty
   - Untouched rows are skipped when saving; "+ Add Row" button is still there
   - Tab from Rate goes to the next row's Buyer; the X buttons are skipped by Tab
3. Row checks before saving (page + server)
   - A row with a buyer or qty must have Date, Buyer, Type, Qty and Rate
   - Message names the row, e.g.  "Row 4: please enter Rate"
   - Editing an old bill shows only that bill's own rows (no blank padding)

Files: static/js/bill-form.js, templates/bill-form.html, dto/BillItemForm.java,
       controller/BillController.java, test/BillItemFormTest.java
