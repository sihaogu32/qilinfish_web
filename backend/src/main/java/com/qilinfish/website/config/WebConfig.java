package com.qilinfish.website.config;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC config: i18n (zh-TW / en). The language comes from the URL (/en/... is English), see LangPathFilter.
 * One message file pair per page: i18n/<page>.properties is Chinese (the default), i18n/<page>_en.properties English.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource source = new ReloadableResourceBundleMessageSource();
        source.setBasenames(
            "classpath:i18n/layout",
            "classpath:i18n/about",
            "classpath:i18n/products",
            "classpath:i18n/cases",
            "classpath:i18n/contact",
            "classpath:i18n/resources",
            "classpath:i18n/blog",
            "classpath:i18n/survey");
        source.setDefaultEncoding("UTF-8");
        // without this, a Chinese request on an English-locale server would pick the _en file
        source.setFallbackToSystemLocale(false);
        source.setUseCodeAsDefaultMessage(true);
        return source;
    }

    @Bean
    public LocaleResolver localeResolver() {
        return new LangLocaleResolver();
    }
}
