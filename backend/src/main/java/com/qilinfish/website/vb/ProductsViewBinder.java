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
    private String activeCategory;       // legal-agent / custom-agent / others
    private List<ProductModel> legalAgentProducts;
    private List<ProductModel> customAgentProducts;
    private List<ProductModel> otherProducts;

    public ProductsViewBinder() { }

    public String getPageTitle() { return pageTitle; }
    public void setPageTitle(String __pageTitle) { this.pageTitle = __pageTitle; }

    public String getPageSubtitle() { return pageSubtitle; }
    public void setPageSubtitle(String __pageSubtitle) { this.pageSubtitle = __pageSubtitle; }

    public String getActiveCategory() { return activeCategory; }
    public void setActiveCategory(String __activeCategory) { this.activeCategory = __activeCategory; }

    public List<ProductModel> getLegalAgentProducts() { return legalAgentProducts; }
    public void setLegalAgentProducts(List<ProductModel> __list) { this.legalAgentProducts = __list; }

    public List<ProductModel> getCustomAgentProducts() { return customAgentProducts; }
    public void setCustomAgentProducts(List<ProductModel> __list) { this.customAgentProducts = __list; }

    public List<ProductModel> getOtherProducts() { return otherProducts; }
    public void setOtherProducts(List<ProductModel> __list) { this.otherProducts = __list; }
}
