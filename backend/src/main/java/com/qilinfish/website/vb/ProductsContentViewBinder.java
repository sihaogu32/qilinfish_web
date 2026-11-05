package com.qilinfish.website.vb;

import java.util.List;

/**
 * ViewBinder for the article-app product page (/products/content) — the "VM" in MVVM.
 * Five parts in order (spec D19): the demo excerpt, why it usually breaks (two rows of steps), real examples,
 * how you pay, where the product stands. Fixed copy comes from i18n/products_content*.properties (pc. keys).
 */
public class ProductsContentViewBinder {

    /** One sentence of the demo excerpt; cite is the article's reference mark after it ("[3]"), or null. */
    public static class Sentence {
        private final String text;
        private final String cite;
        private final String evidence;     // id of the evidence it opens, e.g. e10 (pc.ev.<id>.* keys)

        public Sentence(String __text, String __cite, String __evidence) {
            this.text = __text;
            this.cite = __cite;
            this.evidence = __evidence;
        }

        public String getText() { return text; }
        public String getCite() { return cite; }
        public String getEvidence() { return evidence; }
    }

    /** One quote from the source: the original words, a note on where it is from, and the checked translation (zh page). */
    public static class Quote {
        private final String note;
        private final String original;
        private final String translation;

        public Quote(String __note, String __original, String __translation) {
            this.note = __note;
            this.original = __original;
            this.translation = __translation;
        }

        public String getNote() { return note; }
        public String getOriginal() { return original; }
        public String getTranslation() { return translation; }
    }

    /**
     * The evidence a demo sentence opens (spec B9, B10): written in the site's own copy (pc.ev.<id>.*), not read from
     * the article package. The qualifiers always show with the quotes (D48: in full).
     */
    public static class Evidence {
        private final String id;
        private final String publisher;
        private final String title;
        private final String date;
        private final List<Quote> quotes;
        private final String qualifiers;
        private final String url;

        public Evidence(String __id, String __publisher, String __title, String __date, List<Quote> __quotes,
                        String __qualifiers, String __url) {
            this.id = __id;
            this.publisher = __publisher;
            this.title = __title;
            this.date = __date;
            this.quotes = __quotes;
            this.qualifiers = __qualifiers;
            this.url = __url;
        }

        public String getId() { return id; }
        public String getPublisher() { return publisher; }
        public String getTitle() { return title; }
        public String getDate() { return date; }
        public List<Quote> getQuotes() { return quotes; }
        public String getQualifiers() { return qualifiers; }
        public String getUrl() { return url; }
    }

    private List<Sentence> demo;
    private List<Evidence> evidence;       // one panel per evidence id, in the order the excerpt first uses them
    private List<String> brokenSteps;      // the usual way: one tool per step
    private List<String> lineSteps;        // one line, from the question to the finished piece
    private List<BlogViewBinder.Card> cases;   // articles with evidence marks in the page language; empty hides part 3 (D59)

    public ProductsContentViewBinder() { }

    public List<Sentence> getDemo() { return demo; }
    public void setDemo(List<Sentence> __demo) { this.demo = __demo; }

    public List<Evidence> getEvidence() { return evidence; }
    public void setEvidence(List<Evidence> __evidence) { this.evidence = __evidence; }

    public List<String> getBrokenSteps() { return brokenSteps; }
    public void setBrokenSteps(List<String> __steps) { this.brokenSteps = __steps; }

    public List<String> getLineSteps() { return lineSteps; }
    public void setLineSteps(List<String> __steps) { this.lineSteps = __steps; }

    public List<BlogViewBinder.Card> getCases() { return cases; }
    public void setCases(List<BlogViewBinder.Card> __cases) { this.cases = __cases; }
}
