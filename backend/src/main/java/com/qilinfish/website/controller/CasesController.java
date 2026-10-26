package com.qilinfish.website.controller;

import com.qilinfish.website.vb.CasesViewBinder;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Locale;

@Controller
public class CasesController {

    private final MessageSource messages;

    public CasesController(MessageSource __messages) {
        this.messages = __messages;
    }

    @GetMapping("/cases")
    public String renderCases(Model __model, Locale __locale) {
        CasesViewBinder vb = new CasesViewBinder();
        vb.setPageTitle(messages.getMessage("cases.pageTitle", null, __locale));
        vb.setPageSubtitle(messages.getMessage("cases.pageSubtitle", null, __locale));
        vb.setEmpty(true);             // currently reserved
        vb.setCases(List.of());
        __model.addAttribute("vb", vb);
        __model.addAttribute("activeNav", "cases");
        return "cases";
    }
}
