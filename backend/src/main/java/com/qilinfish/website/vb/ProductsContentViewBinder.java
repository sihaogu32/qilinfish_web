package com.qilinfish.website.vb;

import com.qilinfish.website.model.EvidenceModel;

import java.util.List;

/**
 * ViewBinder for the article-app product page (/products/content) — the "VM" in MVVM.
 * Five parts in order (spec D19): the demo excerpt (a real paragraph read from an article package, article-evidence B14), why it usually breaks (two rows of steps), real examples,
 * how you pay, where the product stands. Fixed copy comes from i18n/products_content*.properties (pc. keys).
 */
public class ProductsContentViewBinder {

    private String demoHtml;               // the demo paragraph from an article package, as it is there (B14); null: no demo
    private List<EvidenceModel> demoEvidence = List.of();   // the evidence its marks point at (same panel as the blog, B15)
    private List<String> brokenSteps;      // the usual way: one tool per step
    private List<String> lineSteps;        // one line, from the question to the finished piece
    private List<BlogViewBinder.Card> cases;   // articles with evidence marks in the page language; empty hides part 3 (D59)

    public ProductsContentViewBinder() { }

    public String getDemoHtml() { return demoHtml; }
    public void setDemoHtml(String __html) { this.demoHtml = __html; }

    public List<EvidenceModel> getDemoEvidence() { return demoEvidence; }
    public void setDemoEvidence(List<EvidenceModel> __evidence) { this.demoEvidence = __evidence; }

    public List<String> getBrokenSteps() { return brokenSteps; }
    public void setBrokenSteps(List<String> __steps) { this.brokenSteps = __steps; }

    public List<String> getLineSteps() { return lineSteps; }
    public void setLineSteps(List<String> __steps) { this.lineSteps = __steps; }

    public List<BlogViewBinder.Card> getCases() { return cases; }
    public void setCases(List<BlogViewBinder.Card> __cases) { this.cases = __cases; }
}
