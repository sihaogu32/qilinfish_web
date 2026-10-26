package com.qilinfish.website.controller;

import com.qilinfish.website.model.ContactFormModel;
import com.qilinfish.website.service.MailService;
import com.qilinfish.website.vb.ContactViewBinder;
import jakarta.validation.Valid;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Locale;

@Controller
public class ContactController {

    private final MailService mailService;
    private final MessageSource messages;

    public ContactController(MailService __mailService, MessageSource __messages) {
        this.mailService = __mailService;
        this.messages = __messages;
    }

    @GetMapping("/contact")
    public String renderContact(Model __model, Locale __locale) {
        __model.addAttribute("vb", buildContactBinder(__locale));
        __model.addAttribute("contactForm", new ContactFormModel());
        __model.addAttribute("activeNav", "contact");
        return "contact";
    }

    @PostMapping("/contact")
    public String submitContact(@Valid @ModelAttribute("contactForm") ContactFormModel __form,
                                BindingResult __bindingResult,
                                Model __model,
                                Locale __locale) {
        __model.addAttribute("vb", buildContactBinder(__locale));
        __model.addAttribute("activeNav", "contact");

        if (__bindingResult.hasErrors()) {
            __model.addAttribute("submitStatus", "error");
            return "contact";
        }
        // "en" or "zh" — tells the owner which language page the message came from
        boolean ok = mailService.sendContactNotification(__form, __locale.getLanguage());
        if (ok) {
            __model.addAttribute("submitStatus", "success");
            __model.addAttribute("contactForm", new ContactFormModel());
        } else {
            __model.addAttribute("submitStatus", "mail-error");
        }
        return "contact";
    }

    private ContactViewBinder buildContactBinder(Locale __locale) {
        ContactViewBinder vb = new ContactViewBinder();
        vb.setPageTitle(messages.getMessage("contact.pageTitle", null, __locale));
        vb.setPageSubtitle(messages.getMessage("contact.pageSubtitle", null, __locale));
        vb.setAddress(messages.getMessage("contact.address", null, __locale));
        vb.setPhone("+886 (0)2-XXXX-XXXX");
        vb.setEmail("qilinfish@qilinfish.com");
        vb.setWorkingHours(messages.getMessage("contact.workingHours", null, __locale));
        return vb;
    }
}
