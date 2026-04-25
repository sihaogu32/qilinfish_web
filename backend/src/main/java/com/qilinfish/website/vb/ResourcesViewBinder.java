package com.qilinfish.website.vb;

import com.qilinfish.website.model.ResourceLinkModel;

import java.util.List;

/**
 * ViewBinder for the resources page.
 */
public class ResourcesViewBinder {

    private String pageTitle;
    private String pageSubtitle;
    private List<ResourceLinkModel> links;

    public ResourcesViewBinder() { }

    public String getPageTitle() { return pageTitle; }
    public void setPageTitle(String __pageTitle) { this.pageTitle = __pageTitle; }

    public String getPageSubtitle() { return pageSubtitle; }
    public void setPageSubtitle(String __pageSubtitle) { this.pageSubtitle = __pageSubtitle; }

    public List<ResourceLinkModel> getLinks() { return links; }
    public void setLinks(List<ResourceLinkModel> __links) { this.links = __links; }
}
