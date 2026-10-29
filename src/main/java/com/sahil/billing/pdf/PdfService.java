package com.sahil.billing.pdf;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.sahil.billing.config.Theme;
import com.sahil.billing.entity.Bill;
import com.sahil.billing.entity.BillItem;
import com.sahil.billing.entity.FirmSettings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Locale;

@Service
public class PdfService {

    /** Rows on a bill that fits on ONE page (no Page Total / page number needed). */
    public static final int SINGLE_PAGE_ROWS = 23;

    /** Rows on every page of a multi-page bill (Page Total + page number take space). */
    public static final int MULTI_PAGE_ROWS = 21;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    @Autowired
    private TemplateEngine templateEngine;

    // ── Bill PDF (19 rows per page, header/footer repeated) ──
    public byte[] billPdf(Bill bill, FirmSettings firm) {
        List<BillItem> all = bill.getItems();

        // Small bill -> one page with more rows. Bigger bill -> pages of MULTI_PAGE_ROWS.
        boolean singlePage = all.size() <= SINGLE_PAGE_ROWS;
        int rowsPerPage    = singlePage ? SINGLE_PAGE_ROWS : MULTI_PAGE_ROWS;
        int totalPages     = singlePage ? 1
                : (int) Math.ceil(all.size() / (double) rowsPerPage);

        List<BillPage> pages = new ArrayList<>();
        for (int p = 0; p < totalPages; p++) {
            int from = p * rowsPerPage;
            int to   = Math.min(from + rowsPerPage, all.size());
            List<BillItem> slice = new ArrayList<>(all.subList(from, to));

            BigDecimal pageTotal = BigDecimal.ZERO;
            for (BillItem it : slice) {
                if (it.getAmount() != null) pageTotal = pageTotal.add(it.getAmount());
            }

            List<Integer> fillers = new ArrayList<>();
            for (int i = slice.size(); i < rowsPerPage; i++) fillers.add(i);

            pages.add(new BillPage(p + 1, totalPages, slice, pageTotal,
                    fillers, p == totalPages - 1));
        }

        Context ctx = new Context(Locale.ENGLISH);
        ctx.setVariable("bill",  bill);
        ctx.setVariable("firm",  firm);
        ctx.setVariable("pages", pages);
        ctx.setVariable("fmt",   FMT);
        ctx.setVariable("theme", Theme.of(firm.getTheme()));
        ctx.setVariable("logoDataUri", logoDataUri(firm));
        return render("pdf/bill", ctx);
    }

    public byte[] reportPdf(List<Bill> bills, FirmSettings firm,
                            String label, BigDecimal grandTotal) {
        Context ctx = new Context(Locale.ENGLISH);
        ctx.setVariable("bills",      bills);
        ctx.setVariable("firm",       firm);
        ctx.setVariable("label",      label);
        ctx.setVariable("grandTotal", grandTotal);
        ctx.setVariable("fmt",        FMT);
        ctx.setVariable("theme",      Theme.of(firm.getTheme()));
        return render("pdf/report", ctx);
    }

    /**
     * Reads the firm's logo image (under static/) and turns it into a data: URI that
     * the PDF renderer can embed. Returns null when the firm has no logo image,
     * in which case the template draws the round initials logo instead.
     */
    private String logoDataUri(FirmSettings firm) {
        String path = firm.getLogoPath();
        if (path == null || path.isBlank()) return null;
        try (InputStream in = new ClassPathResource("static/" + path.trim()).getInputStream()) {
            String lower = path.toLowerCase();
            String mime  = (lower.endsWith(".jpg") || lower.endsWith(".jpeg"))
                           ? "image/jpeg" : "image/png";
            return "data:" + mime + ";base64,"
                   + Base64.getEncoder().encodeToString(in.readAllBytes());
        } catch (Exception e) {
            return null;   // missing file -> fall back to the initials circle
        }
    }

    private byte[] render(String template, Context ctx) {
        String html = templateEngine.process(template, ctx);
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.withHtmlContent(html, null);
            builder.toStream(out);
            builder.run();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("PDF generation failed", e);
        }
    }
}
