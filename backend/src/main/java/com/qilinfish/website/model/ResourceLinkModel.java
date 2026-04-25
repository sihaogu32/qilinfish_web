package com.qilinfish.website.model;

/**
 * Pure data Model — an external reference link entry shown on the resources page.
 */
public class ResourceLinkModel {

    private String id;
    private String label;
    private String url;
    private String description;

    public ResourceLinkModel() { }

    public ResourceLinkModel(String __id,
                             String __label,
                             String __url,
                             String __description) {
        this.id = __id;
        this.label = __label;
        this.url = __url;
        this.description = __description;
    }

    public String getId() { return id; }
    public void setId(String __id) { this.id = __id; }

    public String getLabel() { return label; }
    public void setLabel(String __label) { this.label = __label; }

    public String getUrl() { return url; }
    public void setUrl(String __url) { this.url = __url; }

    public String getDescription() { return description; }
    public void setDescription(String __description) { this.description = __description; }
}
