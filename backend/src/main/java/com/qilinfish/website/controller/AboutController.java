package com.qilinfish.website.controller;

import com.qilinfish.website.vb.AboutViewBinder;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Arrays;
import java.util.Locale;

/**
 * The copy lives in i18n/about.properties (Chinese, default) and i18n/about_en.properties (English).
 */
@Controller
public class AboutController {

    private final MessageSource messages;

    public AboutController(MessageSource __messages) {
        this.messages = __messages;
    }

    @GetMapping("/about")
    public String renderAbout(Model __model, Locale __locale) {
        AboutViewBinder vb = new AboutViewBinder();
        vb.setPageTitle(msg("about.title", __locale));
        vb.setPageSubtitle(msg("about.eyebrow", __locale));
        vb.setStudioName(msg("about.studio", __locale));
        vb.setTagline(msg("about.tagline", __locale));
        // about.what: "||" between clauses, "|" between phrases; a phrase keeps its trailing space
        vb.setValueProposition(splitClauses(msg("about.what", __locale)));
        vb.setStory(new String[] {
            msg("about.story.1", __locale),
            msg("about.story.2", __locale),
            msg("about.story.3", __locale),
            msg("about.story.4", __locale),
            msg("about.story.5", __locale)
        });
        vb.setPhilosophy(new String[] {
            msg("about.way.1", __locale),
            msg("about.way.2", __locale),
            msg("about.way.3", __locale)
        });
        __model.addAttribute("vb", vb);
        __model.addAttribute("activeNav", "about");
        return "about";
    }

    private String msg(String __key, Locale __locale) {
        return messages.getMessage(__key, null, __locale);
    }

    private static String[][] splitClauses(String __text) {
        return Arrays.stream(__text.split("\\|\\|"))
            .map(clause -> clause.split("\\|"))
            .toArray(String[][]::new);
    }
}
