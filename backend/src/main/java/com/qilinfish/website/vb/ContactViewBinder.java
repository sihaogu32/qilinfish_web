package com.qilinfish.website.vb;

/**
 * ViewBinder for the contact page — owns address / phone / email + form state.
 */
public class ContactViewBinder {

    private String pageTitle;
    private String pageSubtitle;
    private String address;
    private String phone;
    private String email;
    private String workingHours;

    public ContactViewBinder() { }

    public String getPageTitle() { return pageTitle; }
    public void setPageTitle(String __pageTitle) { this.pageTitle = __pageTitle; }

    public String getPageSubtitle() { return pageSubtitle; }
    public void setPageSubtitle(String __pageSubtitle) { this.pageSubtitle = __pageSubtitle; }

    public String getAddress() { return address; }
    public void setAddress(String __address) { this.address = __address; }

    public String getPhone() { return phone; }
    public void setPhone(String __phone) { this.phone = __phone; }

    public String getEmail() { return email; }
    public void setEmail(String __email) { this.email = __email; }

    public String getWorkingHours() { return workingHours; }
    public void setWorkingHours(String __workingHours) { this.workingHours = __workingHours; }
}
