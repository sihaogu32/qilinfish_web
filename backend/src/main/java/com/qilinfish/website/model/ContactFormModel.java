package com.qilinfish.website.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Form-binding Model for the contact form.
 * Validation annotations support server-side checks; the front end runs
 * its own validation pass before submission.
 */
public class ContactFormModel {

    @NotBlank
    @Size(max = 64)
    private String name;

    @NotBlank
    @Email
    @Size(max = 128)
    private String email;

    @Size(max = 64)
    private String subject;

    @NotBlank
    @Size(max = 2000)
    private String message;

    public ContactFormModel() { }

    public String getName() { return name; }
    public void setName(String __name) { this.name = __name; }

    public String getEmail() { return email; }
    public void setEmail(String __email) { this.email = __email; }

    public String getSubject() { return subject; }
    public void setSubject(String __subject) { this.subject = __subject; }

    public String getMessage() { return message; }
    public void setMessage(String __message) { this.message = __message; }
}
