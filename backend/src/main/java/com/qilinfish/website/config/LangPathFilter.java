package com.qilinfish.website.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * English pages live under /en/...; this strips the prefix and forwards, so every controller keeps one mapping.
 * The language and the language-free path are left on the request for LangLocaleResolver and LangModelAdvice.
 */
@Component
public class LangPathFilter extends OncePerRequestFilter {

    public static final String ATTR_LANG = "qf.lang";
    public static final String ATTR_PATH = "qf.path";
    public static final String ATTR_QUERY = "qf.query";
    public static final String EN = "en";
    public static final String EN_PREFIX = "/en";

    @Override
    protected void doFilterInternal(HttpServletRequest __request, HttpServletResponse __response, FilterChain __chain)
            throws ServletException, IOException {
        String uri = __request.getRequestURI();
        __request.setAttribute(ATTR_QUERY, __request.getQueryString());
        if (uri.equals(EN_PREFIX) || uri.startsWith(EN_PREFIX + "/")) {
            String rest = uri.substring(EN_PREFIX.length());
            String path = rest.isEmpty() ? "/" : rest;
            __request.setAttribute(ATTR_LANG, EN);
            __request.setAttribute(ATTR_PATH, path);
            __request.getRequestDispatcher(path).forward(__request, __response);
            return;
        }
        __request.setAttribute(ATTR_PATH, uri);
        __chain.doFilter(__request, __response);
    }
}
