package com.sahil.billing.controller;

import com.sahil.billing.config.FirmInterceptor;
import com.sahil.billing.config.Theme;
import com.sahil.billing.entity.FirmSettings;
import com.sahil.billing.repository.FirmSettingsRepository;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

/**
 * "Select Firm" screen. Shown every time the application is opened and whenever
 * the user clicks "Switch Firm". Opening this page clears the current selection.
 */
@Controller
public class FirmController {

    @Autowired private FirmSettingsRepository firmRepo;

    /** A firm together with its colours, for drawing the selection cards. */
    @Getter
    @AllArgsConstructor
    public static class FirmCard {
        private final FirmSettings firm;
        private final Theme theme;
    }

    @GetMapping("/select-firm")
    public String selectFirmPage(HttpSession session, Model model) {
        session.removeAttribute(FirmInterceptor.SESSION_FIRM_ID);

        List<FirmCard> cards = new ArrayList<>();
        for (FirmSettings f : firmRepo.findAll(Sort.by("id"))) {
            cards.add(new FirmCard(f, Theme.of(f.getTheme())));
        }
        model.addAttribute("cards", cards);
        return "select-firm";
    }

    @PostMapping("/select-firm")
    public String chooseFirm(@RequestParam Long firmId, HttpSession session) {
        if (firmRepo.existsById(firmId)) {
            session.setAttribute(FirmInterceptor.SESSION_FIRM_ID, firmId);
            return "redirect:/";
        }
        return "redirect:/select-firm";
    }
}
