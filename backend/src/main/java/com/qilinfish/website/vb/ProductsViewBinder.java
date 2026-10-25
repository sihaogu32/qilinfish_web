package com.qilinfish.website.vb;

import com.qilinfish.website.model.ProductModel;

import java.util.List;

/**
 * ViewBinder for the products page — the "VM" in MVVM.
 * Wraps the raw Model list with view-only fields (active tab, header copy).
 */
public class ProductsViewBinder {

    private String pageTitle;
    private String pageSubtitle;
    private String activeCategory;       // ai-audit / others
    private List<ProductModel> aiAuditProducts;
    private List<ProductModel> otherProducts;

    public ProductsViewBinder() { }

    public String getPageTitle() { return pageTitle; }
    public void setPageTitle(String __pageTitle) { this.pageTitle = __pageTitle; }

    public String getPageSubtitle() { return pageSubtitle; }
    public void setPageSubtitle(String __pageSubtitle) { this.pageSubtitle = __pageSubtitle; }

    public String getActiveCategory() { return activeCategory; }
    public void setActiveCategory(String __activeCategory) { this.activeCategory = __activeCategory; }

    public List<ProductModel> getAiAuditProducts() { return aiAuditProducts; }
    public void setAiAuditProducts(List<ProductModel> __list) { this.aiAuditProducts = __list; }

    public List<ProductModel> getOtherProducts() { return otherProducts; }
    public void setOtherProducts(List<ProductModel> __list) { this.otherProducts = __list; }
}
