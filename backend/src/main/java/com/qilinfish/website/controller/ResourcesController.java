package com.qilinfish.website.controller;

import com.qilinfish.website.model.ResourceLinkModel;
import com.qilinfish.website.vb.ResourcesViewBinder;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Locale;

@Controller
public class ResourcesController {

    private final MessageSource messages;

    public ResourcesController(MessageSource __messages) {
        this.messages = __messages;
    }

    @GetMapping("/resources")
    public String renderResources(Model __model, Locale __locale) {
        ResourcesViewBinder vb = new ResourcesViewBinder();
        vb.setPageTitle(messages.getMessage("resources.pageTitle", null, __locale));
        vb.setPageSubtitle(messages.getMessage("resources.pageSubtitle", null, __locale));
        vb.setLinks(List.of(
            new ResourceLinkModel(
                "r-01",
                messages.getMessage("resources.link.law.label", null, __locale),
                "https://law.moj.gov.tw/",
                messages.getMessage("resources.link.law.description", null, __locale)
            )
        ));
        __model.addAttribute("vb", vb);
        __model.addAttribute("activeNav", "resources");
        return "resources";
    }
}
