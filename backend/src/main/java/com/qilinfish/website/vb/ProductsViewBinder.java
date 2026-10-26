package com.qilinfish.website.vb;

import com.qilinfish.website.model.ScenarioModel;

import java.util.List;

/**
 * ViewBinder for the products page — the "VM" in MVVM.
 * Wraps the raw scenario list with view-only fields (hero copy).
 */
public class ProductsViewBinder {

    private String heroTitle;
    private String heroSubtitle;
    private List<ScenarioModel> scenarios;

    public ProductsViewBinder() { }

    public String getHeroTitle() { return heroTitle; }
    public void setHeroTitle(String __heroTitle) { this.heroTitle = __heroTitle; }

    public String getHeroSubtitle() { return heroSubtitle; }
    public void setHeroSubtitle(String __heroSubtitle) { this.heroSubtitle = __heroSubtitle; }

    public List<ScenarioModel> getScenarios() { return scenarios; }
    public void setScenarios(List<ScenarioModel> __list) { this.scenarios = __list; }
}
