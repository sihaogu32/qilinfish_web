package com.qilinfish.website.vb;

/**
 * ViewBinder for the survey page — header copy only; form state lives in SurveyFormModel.
 */
public class SurveyViewBinder {

    private String pageTitle;
    private String pageSubtitle;

    public SurveyViewBinder() { }

    public String getPageTitle() { return pageTitle; }
    public void setPageTitle(String __pageTitle) { this.pageTitle = __pageTitle; }

    public String getPageSubtitle() { return pageSubtitle; }
    public void setPageSubtitle(String __pageSubtitle) { this.pageSubtitle = __pageSubtitle; }
}
