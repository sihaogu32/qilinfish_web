package com.qilinfish.website.controller;

import com.qilinfish.website.model.ContactFormModel;
import com.qilinfish.website.service.MailService;
import com.qilinfish.website.vb.ContactViewBinder;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class ContactController {

    private final MailService mailService;

    public ContactController(MailService __mailService) {
        this.mailService = __mailService;
    }

    @GetMapping("/contact")
    public String renderContact(Model __model) {
        __model.addAttribute("vb", buildContactBinder());
        __model.addAttribute("contactForm", new ContactFormModel());
        __model.addAttribute("activeNav", "contact");
        return "contact";
    }

    @PostMapping("/contact")
    public String submitContact(@Valid @ModelAttribute("contactForm") ContactFormModel __form,
                                BindingResult __bindingResult,
                                Model __model) {
        __model.addAttribute("vb", buildContactBinder());
        __model.addAttribute("activeNav", "contact");

        if (__bindingResult.hasErrors()) {
            __model.addAttribute("submitStatus", "error");
            return "contact";
        }
        boolean ok = mailService.sendContactNotification(__form);
        if (ok) {
            __model.addAttribute("submitStatus", "success");
            __model.addAttribute("contactForm", new ContactFormModel());
        } else {
            __model.addAttribute("submitStatus", "mail-error");
        }
        return "contact";
    }

    private ContactViewBinder buildContactBinder() {
        ContactViewBinder vb = new ContactViewBinder();
        vb.setPageTitle("聯絡我們");
        vb.setPageSubtitle("CONTACT // get in touch");
        vb.setAddress("台北市XX區");
        vb.setPhone("+886 (0)2-XXXX-XXXX");
        vb.setEmail("qilinfish@qilinfish.com");
        vb.setWorkingHours("週一 — 週五  10:00 / 19:00 (UTC+8)");
        return vb;
    }
}
