package com.sahil.billing.controller;

import com.sahil.billing.config.FirmContext;
import com.sahil.billing.config.Theme;
import com.sahil.billing.entity.FirmSettings;
import com.sahil.billing.repository.FirmSettingsRepository;
import com.sahil.billing.service.BillService;
import com.sahil.billing.util.DateRangeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class HomeController {

    @Autowired private BillService            billService;
    @Autowired private FirmSettingsRepository firmRepo;

    @GetMapping("/")
    public String home(Model model) {
        FirmSettings firm = FirmContext.requireFirm();

        model.addAttribute("recentBills", billService.recent(5));
        model.addAttribute("firm", firm);
        model.addAttribute("currentFyYear", DateRangeUtil.currentFyYear());
        return "home";
    }

    // ── Settings (of the selected firm) ──────────────────────
    @GetMapping("/settings")
    public String settingsPage(Model model) {
        model.addAttribute("firm", FirmContext.requireFirm());
        model.addAttribute("themes", Theme.all());
        return "settings";
    }

    @PostMapping("/settings")
    public String saveSettings(@ModelAttribute FirmSettings form,
                               RedirectAttributes flash) {
        FirmSettings current = FirmContext.requireFirm();

        // A settings page left open for another firm must never overwrite this firm.
        if (form.getId() == null || !form.getId().equals(current.getId())) {
            flash.addFlashAttribute("error",
                "The firm was changed after this page was opened. Please try again.");
            return "redirect:/settings";
        }

        FirmSettings firm = firmRepo.findById(current.getId()).orElseThrow();
        firm.setFirmName(form.getFirmName());
        firm.setSubtitle(form.getSubtitle());
        firm.setAddress(form.getAddress());
        firm.setMobile(form.getMobile());
        firm.setPanNumber(form.getPanNumber());
        firm.setFooterLine1(form.getFooterLine1());
        firm.setFooterLine2(form.getFooterLine2());
        firm.setLogoText(form.getLogoText());
        if (Theme.isKnown(form.getTheme())) {
            firm.setTheme(form.getTheme().trim().toUpperCase());
        }
        firmRepo.save(firm);
        flash.addFlashAttribute("success", "Settings saved successfully!");
        return "redirect:/settings";
    }
}
