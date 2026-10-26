package com.qilinfish.website.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Language fields every template uses:
 *   lp       — link prefix, "" or "/en"; write site links as th:href="${lp + '/survey'}"
 *   htmlLang — value for <html th:lang>
 *   zhUrl / enUrl — this same page in each language, for the switcher and hreflang
 */
@ControllerAdvice
public class LangModelAdvice {

    @ModelAttribute
    public void addLangFields(HttpServletRequest __request, Model __model) {
        boolean en = LangLocaleResolver.isEnglish(__request);
        Object pathAttr = __request.getAttribute(LangPathFilter.ATTR_PATH);
        String path = pathAttr == null ? __request.getRequestURI() : pathAttr.toString();
        Object query = __request.getAttribute(LangPathFilter.ATTR_QUERY);
        String suffix = query == null ? "" : "?" + query;
        __model.addAttribute("lp", en ? LangPathFilter.EN_PREFIX : "");
        __model.addAttribute("htmlLang", en ? "en" : "zh-Hant-TW");
        __model.addAttribute("isEn", en);
        __model.addAttribute("zhUrl", path + suffix);
        __model.addAttribute("enUrl", LangPathFilter.EN_PREFIX + path + suffix);
    }
}
