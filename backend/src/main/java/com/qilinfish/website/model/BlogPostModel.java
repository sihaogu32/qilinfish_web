package com.qilinfish.website.model;

/**
 * Pure data Model — one blog post entry (list card + post header).
 * The post body itself lives in templates/blog/<slug>.html.
 */
public class BlogPostModel {

    private String slug;       // URL part (/blog/<slug>) and body template name (blog/<slug>.html)
    private String title;
    private String summary;
    private String date;       // yyyy-MM-dd
    private String cover;      // file stem under /media: <cover>.jpg

    public BlogPostModel() { }

    public BlogPostModel(String __slug,
                         String __title,
                         String __summary,
                         String __date,
                         String __cover) {
        this.slug = __slug;
        this.title = __title;
        this.summary = __summary;
        this.date = __date;
        this.cover = __cover;
    }

    public String getSlug() { return slug; }
    public void setSlug(String __slug) { this.slug = __slug; }

    public String getTitle() { return title; }
    public void setTitle(String __title) { this.title = __title; }

    public String getSummary() { return summary; }
    public void setSummary(String __summary) { this.summary = __summary; }

    public String getDate() { return date; }
    public void setDate(String __date) { this.date = __date; }

    public String getCover() { return cover; }
    public void setCover(String __cover) { this.cover = __cover; }
}
