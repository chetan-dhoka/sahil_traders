package com.sahil.billing.controller;

import com.opencsv.CSVWriter;
import com.sahil.billing.config.FirmContext;
import com.sahil.billing.entity.Bill;
import com.sahil.billing.entity.FirmSettings;
import com.sahil.billing.entity.Seller;
import com.sahil.billing.pdf.PdfService;
import com.sahil.billing.repository.SellerRepository;
import com.sahil.billing.service.BillService;
import com.sahil.billing.util.DateRangeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@RequestMapping("/reports")
public class ReportController {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    @Autowired private BillService      billService;
    @Autowired private PdfService       pdfService;
    @Autowired private SellerRepository sellerRepo;

    @GetMapping
    public String reports(
            @RequestParam(defaultValue = "yearly") String period,
            @RequestParam(required = false)         Integer fyYear,
            @RequestParam(required = false)         Long    sellerId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate toDate,
            Model model) {

        DateRangeUtil.DateRange range =
            DateRangeUtil.resolve(period, fyYear, fromDate, toDate);

        List<Bill> bills = (sellerId != null)
            ? billService.findBySellerAndDateRange(sellerId, range.from, range.to)
            : billService.findByDateRange(range.from, range.to);

        BigDecimal grandTotal = bills.stream()
            .map(Bill::getTotalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        int currentFy = DateRangeUtil.currentFyYear();

        model.addAttribute("bills",       bills);
        model.addAttribute("grandTotal",  grandTotal);
        model.addAttribute("period",      period);
        model.addAttribute("fyYear",      fyYear != null ? fyYear : currentFy);
        model.addAttribute("currentFy",   currentFy);
        model.addAttribute("rangeLabel",  range.label);
        model.addAttribute("fromDate",    range.from.format(FMT));
        model.addAttribute("toDate",      range.to.format(FMT));
        model.addAttribute("sellers",     sellerRepo.findAllActive());
        model.addAttribute("sellerId",    sellerId);
        return "reports";
    }

    // ── PDF ──────────────────────────────────────────────────
    @GetMapping("/pdf")
    public ResponseEntity<byte[]> pdf(
            @RequestParam(defaultValue = "yearly") String period,
            @RequestParam(required = false)         Integer fyYear,
            @RequestParam(required = false)         Long    sellerId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate toDate) {

        DateRangeUtil.DateRange range =
            DateRangeUtil.resolve(period, fyYear, fromDate, toDate);
        List<Bill> bills = (sellerId != null)
            ? billService.findBySellerAndDateRange(sellerId, range.from, range.to)
            : billService.findByDateRange(range.from, range.to);

        BigDecimal grandTotal = bills.stream()
            .map(Bill::getTotalAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

        FirmSettings firm = FirmContext.requireFirm();
        byte[] data = pdfService.reportPdf(bills, firm, range.label, grandTotal);

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"report-" + range.label.replace(" ", "-") + ".pdf\"")
            .contentType(MediaType.APPLICATION_PDF)
            .body(data);
    }

    // ── CSV ──────────────────────────────────────────────────
    @GetMapping("/csv")
    public ResponseEntity<byte[]> csv(
            @RequestParam(defaultValue = "yearly") String period,
            @RequestParam(required = false)         Integer fyYear,
            @RequestParam(required = false)         Long    sellerId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate toDate)
            throws Exception {

        DateRangeUtil.DateRange range =
            DateRangeUtil.resolve(period, fyYear, fromDate, toDate);
        List<Bill> bills = (sellerId != null)
            ? billService.findBySellerAndDateRange(sellerId, range.from, range.to)
            : billService.findByDateRange(range.from, range.to);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (CSVWriter w = new CSVWriter(
                new OutputStreamWriter(out, StandardCharsets.UTF_8))) {
            w.writeNext(new String[]{"Bill No", "Date", "Seller", "Total Amount (₹)"});
            for (Bill b : bills) {
                w.writeNext(new String[]{
                    String.valueOf(b.getBillNo()),
                    b.getBillDate().format(FMT),
                    b.getSeller().getName(),
                    b.getTotalAmount().toPlainString()
                });
            }
        }
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"report.csv\"")
            .contentType(MediaType.parseMediaType("text/csv"))
            .body(out.toByteArray());
    }
}
