package com.sahil.billing.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.format.datetime.standard.DateTimeFormatterRegistrar;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.format.DateTimeFormatter;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private FirmInterceptor firmInterceptor;

    @Override
    public void addFormatters(FormatterRegistry registry) {
        DateTimeFormatterRegistrar r = new DateTimeFormatterRegistrar();
        r.setDateFormatter(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        r.registerFormatters(registry);
    }

    /** Every page needs a selected firm, except the selection screen and static files. */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(firmInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/select-firm", "/js/**", "/css/**",
                                     "/logos/**", "/images/**", "/error", "/favicon.ico");
    }
}
