package com.qilinfish.website.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.LocaleResolver;

import java.util.Locale;

/**
 * The URL decides the language: /en/... is English, everything else Traditional Chinese.
 */
public class LangLocaleResolver implements LocaleResolver {

    public static boolean isEnglish(HttpServletRequest __request) {
        return LangPathFilter.EN.equals(__request.getAttribute(LangPathFilter.ATTR_LANG));
    }

    @Override
    public Locale resolveLocale(HttpServletRequest __request) {
        return isEnglish(__request) ? Locale.ENGLISH : Locale.TRADITIONAL_CHINESE;
    }

    @Override
    public void setLocale(HttpServletRequest __request, HttpServletResponse __response, Locale __locale) {
        throw new UnsupportedOperationException("The language comes from the URL path");
    }
}
