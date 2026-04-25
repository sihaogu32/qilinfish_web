package com.qilinfish.website.vb;

import com.qilinfish.website.model.CaseModel;

import java.util.List;

/**
 * ViewBinder for the success-cases page.
 */
public class CasesViewBinder {

    private String pageTitle;
    private String pageSubtitle;
    private boolean empty;
    private List<CaseModel> cases;

    public CasesViewBinder() { }

    public String getPageTitle() { return pageTitle; }
    public void setPageTitle(String __pageTitle) { this.pageTitle = __pageTitle; }

    public String getPageSubtitle() { return pageSubtitle; }
    public void setPageSubtitle(String __pageSubtitle) { this.pageSubtitle = __pageSubtitle; }

    public boolean isEmpty() { return empty; }
    public void setEmpty(boolean __empty) { this.empty = __empty; }

    public List<CaseModel> getCases() { return cases; }
    public void setCases(List<CaseModel> __cases) { this.cases = __cases; }
}
