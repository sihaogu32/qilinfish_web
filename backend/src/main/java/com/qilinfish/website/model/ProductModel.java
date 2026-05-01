package com.qilinfish.website.model;

/**
 * Pure data Model — a product offering shown on the products page.
 * No view-state, no formatting; that lives in the corresponding ViewBinder.
 */
public class ProductModel {

    private String id;
    private String code;       // e.g. "legal-agent"
    private String title;
    private String subtitle;
    private String description;
    private String[] features;
    private String status;     // available / demo-ready / coming-soon / custom

    public ProductModel() { }

    public ProductModel(String __id,
                        String __code,
                        String __title,
                        String __subtitle,
                        String __description,
                        String[] __features,
                        String __status) {
        this.id = __id;
        this.code = __code;
        this.title = __title;
        this.subtitle = __subtitle;
        this.description = __description;
        this.features = __features;
        this.status = __status;
    }

    public String getId() { return id; }
    public void setId(String __id) { this.id = __id; }

    public String getCode() { return code; }
    public void setCode(String __code) { this.code = __code; }

    public String getTitle() { return title; }
    public void setTitle(String __title) { this.title = __title; }

    public String getSubtitle() { return subtitle; }
    public void setSubtitle(String __subtitle) { this.subtitle = __subtitle; }

    public String getDescription() { return description; }
    public void setDescription(String __description) { this.description = __description; }

    public String[] getFeatures() { return features; }
    public void setFeatures(String[] __features) { this.features = __features; }

    public String getStatus() { return status; }
    public void setStatus(String __status) { this.status = __status; }
}
