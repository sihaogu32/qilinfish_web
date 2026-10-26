package com.qilinfish.website.model;

/**
 * Pure data Model — one blog post entry (list card + post header).
 * The post body itself lives in templates/blog/<slug>.html (Chinese) and templates/blog/en/<slug>.html (English).
 * A post ships in both languages or not at all (owner decision D8), so the English title and summary are required.
 */
public class BlogPostModel {

    private String slug;       // URL part (/blog/<slug>, /en/blog/<slug>) and body template name
    private String title;      // Chinese
    private String titleEn;
    private String summary;    // Chinese
    private String summaryEn;
    private String date;       // yyyy-MM-dd
    private String cover;      // file stem under /media: <cover>.jpg

    public BlogPostModel() { }

    public BlogPostModel(String __slug,
                         String __title,
                         String __titleEn,
                         String __summary,
                         String __summaryEn,
                         String __date,
                         String __cover) {
        this.slug = __slug;
        this.title = require(__title, __slug, "title");
        this.titleEn = require(__titleEn, __slug, "titleEn");
        this.summary = require(__summary, __slug, "summary");
        this.summaryEn = require(__summaryEn, __slug, "summaryEn");
        this.date = __date;
        this.cover = __cover;
    }

    private static String require(String __value, String __slug, String __field) {
        if (__value == null || __value.isBlank()) {
            throw new IllegalArgumentException("Blog post '" + __slug + "' needs " + __field
                + " (a post ships in both languages or not at all)");
        }
        return __value;
    }

    /** Title in the requested language. */
    public String title(boolean __en) { return __en ? titleEn : title; }

    /** Summary in the requested language. */
    public String summary(boolean __en) { return __en ? summaryEn : summary; }

    public String getSlug() { return slug; }
    public void setSlug(String __slug) { this.slug = __slug; }

    public String getTitle() { return title; }
    public void setTitle(String __title) { this.title = __title; }

    public String getTitleEn() { return titleEn; }
    public void setTitleEn(String __titleEn) { this.titleEn = __titleEn; }

    public String getSummary() { return summary; }
    public void setSummary(String __summary) { this.summary = __summary; }

    public String getSummaryEn() { return summaryEn; }
    public void setSummaryEn(String __summaryEn) { this.summaryEn = __summaryEn; }

    public String getDate() { return date; }
    public void setDate(String __date) { this.date = __date; }

    public String getCover() { return cover; }
    public void setCover(String __cover) { this.cover = __cover; }
}
