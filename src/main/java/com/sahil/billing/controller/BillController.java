package com.sahil.billing.controller;

import com.sahil.billing.config.FirmContext;
import com.sahil.billing.dto.BillForm;
import com.sahil.billing.dto.BillItemForm;
import com.sahil.billing.entity.Bill;
import com.sahil.billing.entity.BillItem;
import com.sahil.billing.entity.FirmSettings;
import com.sahil.billing.pdf.PdfService;
import com.sahil.billing.service.BillService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@RequestMapping("/bills")
public class BillController {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    @Autowired private BillService billService;
    @Autowired private PdfService  pdfService;

    private static final String FIRM_CHANGED_MSG =
        "The firm was changed after this form was opened, so nothing was saved. "
      + "Please open the form again.";

    /**
     * Checks every row the user has started (buyer or qty typed).  Row numbers are the
     * ones shown on screen, so this runs BEFORE empty rows are removed.
     * Completely empty rows are ignored here and skipped on save.
     */
    private void validateRows(BillForm form, BindingResult result) {
        List<BillItemForm> items = form.getItems();
        for (int i = 0; i < items.size(); i++) {
            String problem = items.get(i).problem();
            if (problem != null) {
                result.reject("row.invalid", "Row " + (i + 1) + ": " + problem);
            }
        }
    }

    /** Called only when the form has to be shown again: always keep at least one row. */
    private void ensureOneRow(BillForm form) {
        if (form.getItems().isEmpty()) form.addEmptyItem();
    }

    /** True when a form was opened for a different firm than the one selected now. */
    private boolean wrongFirm(BillForm form) {
        return form.getFirmId() == null
            || !form.getFirmId().equals(FirmContext.requireFirmId());
    }

    // ── List / Search ────────────────────────────────────────
    @GetMapping
    public String list(
            @RequestParam(required = false) Long   billNo,
            @RequestParam(required = false) String sellerName,
            @RequestParam(required = false) @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate toDate,
            @RequestParam(defaultValue = "0") int page,
            Model model) {

        Page<Bill> bills = billService.search(billNo, sellerName, fromDate, toDate, page);
        model.addAttribute("bills",      bills);
        model.addAttribute("billNo",     billNo);
        model.addAttribute("sellerName", sellerName);
        model.addAttribute("fromDate",   fromDate != null ? fromDate.format(FMT) : "");
        model.addAttribute("toDate",     toDate   != null ? toDate.format(FMT)   : "");
        return "bill-list";
    }

    // ── New Bill Form ────────────────────────────────────────
    @GetMapping("/new")
    public String newForm(Model model) {
        BillForm form = new BillForm();
        form.setFirmId(FirmContext.requireFirmId());
        form.setBillDate(LocalDate.now());
        form.addEmptyItem(); // just 1 row to start
        model.addAttribute("form", form);
        model.addAttribute("isNew", true);
        return "bill-form";
    }

    // ── Save New Bill ────────────────────────────────────────
    // Change redirect to bills list instead of bill view
// OR use findById which does a fresh fetch with open session

    @PostMapping("/new")
    public String create(@Valid @ModelAttribute("form") BillForm form,
                         BindingResult result,
                         Model model,
                         RedirectAttributes flash) {
        if (wrongFirm(form)) {
            flash.addFlashAttribute("error", FIRM_CHANGED_MSG);
            return "redirect:/";
        }
        validateRows(form, result);
        if (!result.hasErrors()) {
            form.getItems().removeIf(BillItemForm::isEmpty);   // skip untouched rows
            if (form.getItems().isEmpty()) {
                result.rejectValue("items", "empty", "Add at least one row");
            }
        }
        if (result.hasErrors()) {
            ensureOneRow(form);
            model.addAttribute("isNew", true);
            return "bill-form";
        }
        Bill bill = billService.create(form);
        flash.addFlashAttribute("success",
                "Bill #" + bill.getBillNo() + " created successfully!");
        return "redirect:/bills/" + bill.getId();  // this is fine - fix is in findById
    }

    // ── View Bill ────────────────────────────────────────────
    @GetMapping("/{id}")
    public String view(@PathVariable Long id, Model model) {
        Bill bill = billService.findById(id)
                .orElseThrow(() -> new RuntimeException("Bill not found"));
        model.addAttribute("bill", bill);
        return "bill-view";
    }

    // ── Edit Bill Form ───────────────────────────────────────
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Bill bill = billService.findById(id)
                .orElseThrow(() -> new RuntimeException("Bill not found"));
        BillForm form = toForm(bill);
        model.addAttribute("form",   form);
        model.addAttribute("billId", id);
        model.addAttribute("billNo", bill.getBillNo());
        model.addAttribute("isNew",  false);
        return "bill-form";
    }

    // ── Save Edit ────────────────────────────────────────────
    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("form") BillForm form,
                         BindingResult result,
                         Model model,
                         RedirectAttributes flash) {
        if (wrongFirm(form)) {
            flash.addFlashAttribute("error", FIRM_CHANGED_MSG);
            return "redirect:/";
        }
        validateRows(form, result);
        if (!result.hasErrors()) {
            form.getItems().removeIf(BillItemForm::isEmpty);   // skip untouched rows
            if (form.getItems().isEmpty()) {
                result.rejectValue("items", "empty", "Add at least one row");
            }
        }
        if (result.hasErrors()) {
            ensureOneRow(form);
            model.addAttribute("isNew", false);
            model.addAttribute("billId", id);
            // ✅ Need billNo for the card header title
            billService.findById(id).ifPresent(b ->
                    model.addAttribute("billNo", b.getBillNo()));
            return "bill-form";
        }
        Bill bill = billService.update(id, form);
        flash.addFlashAttribute("success",
                "Bill #" + bill.getBillNo() + " updated successfully!");
        return "redirect:/bills/" + bill.getId();
    }

    // ── PDF Download ─────────────────────────────────────────
    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> pdf(@PathVariable Long id) {
        Bill bill = billService.findById(id)
                .orElseThrow(() -> new RuntimeException("Bill not found"));
        FirmSettings firm = FirmContext.requireFirm();

        // Generate filename: SellerName-Month-Year.pdf
        String month = bill.getBillDate().getMonth().getDisplayName(
                java.time.format.TextStyle.FULL,
                java.util.Locale.ENGLISH);
        String year       = String.valueOf(bill.getBillDate().getYear());
        String sellerName = bill.getSeller().getName();
        String filename   = sellerName + "-" + month + "-" + year + ".pdf";

        byte[] data = pdfService.billPdf(bill, firm);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(data);
    }

    // ── Delete ───────────────────────────────────────────────
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes flash) {
        billService.delete(id);
        flash.addFlashAttribute("success", "Bill deleted.");
        return "redirect:/bills";
    }

    // ── Helper: entity → form ────────────────────────────────
    private BillForm toForm(Bill bill) {
        BillForm form = new BillForm();
        form.setId(bill.getId());
        form.setFirmId(bill.getFirmId());
        form.setBillDate(bill.getBillDate());
        form.setSellerName(bill.getSeller().getName());
        for (BillItem item : bill.getItems()) {
            BillItemForm f = new BillItemForm();
            f.setItemDate(item.getItemDate());
            f.setBuyerName(item.getBuyer().getName());
            f.setMaterialType(item.getMaterialType());
            f.setQty(item.getQty());
            f.setRate(item.getRate());
            form.getItems().add(f);
        }
        return form;
    }
}
