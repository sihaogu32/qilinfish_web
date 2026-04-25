package com.qilinfish.website.vb;

/**
 * ViewBinder for the about page — domain capabilities & studio narrative.
 */
public class AboutViewBinder {

    private String pageTitle;
    private String pageSubtitle;
    private String studioName;
    private String tagline;
    private String[] capabilities;
    private String[] philosophy;

    public AboutViewBinder() { }

    public String getPageTitle() { return pageTitle; }
    public void setPageTitle(String __pageTitle) { this.pageTitle = __pageTitle; }

    public String getPageSubtitle() { return pageSubtitle; }
    public void setPageSubtitle(String __pageSubtitle) { this.pageSubtitle = __pageSubtitle; }

    public String getStudioName() { return studioName; }
    public void setStudioName(String __studioName) { this.studioName = __studioName; }

    public String getTagline() { return tagline; }
    public void setTagline(String __tagline) { this.tagline = __tagline; }

    public String[] getCapabilities() { return capabilities; }
    public void setCapabilities(String[] __capabilities) { this.capabilities = __capabilities; }

    public String[] getPhilosophy() { return philosophy; }
    public void setPhilosophy(String[] __philosophy) { this.philosophy = __philosophy; }
}
