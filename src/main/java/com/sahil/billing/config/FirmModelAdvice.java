package com.sahil.billing.config;

import com.sahil.billing.entity.FirmSettings;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/** Makes the selected firm and its colours available to every Thymeleaf page. */
@ControllerAdvice
public class FirmModelAdvice {

    @ModelAttribute("currentFirm")
    public FirmSettings currentFirm() {
        return FirmContext.get();
    }

    @ModelAttribute("theme")
    public Theme theme() {
        FirmSettings f = FirmContext.get();
        return Theme.of(f == null ? null : f.getTheme());
    }
}
