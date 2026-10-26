package com.qilinfish.website.controller;

import com.qilinfish.website.config.LangLocaleResolver;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Collections;
import java.util.Locale;

/**
 * The site has no separate home page; "/" lands on the about page.
 * Only the bare "/" picks a language: the switcher's cookie first, then the browser's Accept-Language.
 */
@Controller
public class HomeController {

    @GetMapping({"/", "/index"})
    public String renderHome(HttpServletRequest __request,
                             @CookieValue(name = "qf_lang", required = false) String __cookieLang) {
        if (LangLocaleResolver.isEnglish(__request)) {
            return "redirect:/en/about";
        }
        return pickEnglish(__request, __cookieLang) ? "redirect:/en/about" : "redirect:/about";
    }

    private boolean pickEnglish(HttpServletRequest __request, String __cookieLang) {
        if ("en".equals(__cookieLang)) return true;
        if ("zh".equals(__cookieLang)) return false;
        // without the header getLocales() returns the server's own locale, so crawlers would land on English
        if (__request.getHeader("Accept-Language") == null) return false;
        // first of zh / en in the browser's preference order wins; neither listed keeps the Chinese default
        for (Locale l : Collections.list(__request.getLocales())) {
            if ("zh".equals(l.getLanguage())) return false;
            if ("en".equals(l.getLanguage())) return true;
        }
        return false;
    }
}
