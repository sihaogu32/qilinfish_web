package com.qilinfish.website.service;

import com.qilinfish.website.model.ContactFormModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Sends contact-form notifications.
 *
 * Wire-up:
 *   1. Spring Boot composes the email and dispatches it via SMTP (Gmail)
 *      using the credentials in MAIL_USERNAME / MAIL_PASSWORD.
 *   2. The recipient address (MAIL_TO) is a domain address such as
 *      qilinfish@qilinfish.com.
 *   3. Cloudflare Email Routing receives mail at the qilinfish.com MX
 *      and forwards it to your real Gmail inbox.
 *
 * Note: Cloudflare Email Routing does NOT provide outbound SMTP — that's
 * why we still use Gmail SMTP for the *send* leg.
 */
@Service
public class MailService {

    private static final Logger log = LoggerFactory.getLogger(MailService.class);

    private final JavaMailSender mailSender;

    @Value("${qilinfish.mail.from}")
    private String fromAddress;

    @Value("${qilinfish.mail.to}")
    private String toAddress;

    @Value("${qilinfish.mail.enabled:true}")
    private boolean mailEnabled;

    public MailService(JavaMailSender __mailSender) {
        this.mailSender = __mailSender;
    }

    /**
     * Send a contact-form submission as a notification email.
     * Returns true on success, false on any failure (logged).
     */
    public boolean sendContactNotification(ContactFormModel __form) {
        if (!mailEnabled) {
            log.info("Mail disabled — skipping contact notification from {}", __form.getEmail());
            return true;
        }
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(fromAddress);
            msg.setTo(toAddress);
            msg.setReplyTo(__form.getEmail());
            msg.setSubject(buildSubject(__form));
            msg.setText(buildBody(__form));
            mailSender.send(msg);
            return true;
        } catch (Exception ex) {
            log.error("Failed to send contact notification", ex);
            return false;
        }
    }

    private String buildSubject(ContactFormModel __form) {
        String subj = __form.getSubject();
        if (subj == null || subj.isBlank()) {
            subj = "(no subject)";
        }
        return "[qilinfish.com] " + subj + " — from " + __form.getName();
    }

    private String buildBody(ContactFormModel __form) {
        return "// New contact-form submission\n"
             + "==============================\n"
             + "Name    : " + __form.getName()    + "\n"
             + "Email   : " + __form.getEmail()   + "\n"
             + "Subject : " + (__form.getSubject() == null ? "" : __form.getSubject()) + "\n"
             + "------------------------------\n"
             + __form.getMessage() + "\n"
             + "==============================\n"
             + "// reply-to is set; just hit reply.\n";
    }
}
