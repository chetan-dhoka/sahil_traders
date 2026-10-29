package com.sahil.billing.config;

import com.sahil.billing.entity.FirmSettings;
import com.sahil.billing.repository.FirmSettingsRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Optional;

/**
 * Runs before every page. If no firm has been selected yet, the user is sent to the
 * "Select Firm" screen. Otherwise the selected firm is made available to the request.
 */
@Component
public class FirmInterceptor implements HandlerInterceptor {

    public static final String SESSION_FIRM_ID = "selectedFirmId";

    @Autowired
    private FirmSettingsRepository firmRepo;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {
        FirmContext.clear();

        HttpSession session = request.getSession(false);
        Long firmId = (session == null) ? null : (Long) session.getAttribute(SESSION_FIRM_ID);

        if (firmId != null) {
            Optional<FirmSettings> firm = firmRepo.findById(firmId);
            if (firm.isPresent()) {
                FirmContext.set(firm.get());
                return true;
            }
            session.removeAttribute(SESSION_FIRM_ID);
        }

        response.sendRedirect(request.getContextPath() + "/select-firm");
        return false;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        FirmContext.clear();
    }
}
