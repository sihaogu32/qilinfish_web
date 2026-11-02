package com.qilinfish.website.vb;

import com.qilinfish.website.model.ArticlePackageModel;

import java.util.List;

/**
 * ViewBinder for the blog list and post pages — the "VM" in MVVM.
 * Wraps the article package with view-only fields (page language, list cards, resolved media and page URLs).
 * Fixed header copy comes from i18n/blog.properties, not from here.
 */
public class BlogViewBinder {

    /** One card on the list page: an article package in the page language. */
    public static class Card {
        private final String slug;
        private final String title;
        private final String summary;
        private final String date;
        private final String coverUrl;

        public Card(String __slug, String __title, String __summary, String __date, String __coverUrl) {
            this.slug = __slug;
            this.title = __title;
            this.summary = __summary;
            this.date = __date;
            this.coverUrl = __coverUrl;
        }

        public String getSlug() { return slug; }
        public String getTitle() { return title; }
        public String getSummary() { return summary; }
        public String getDate() { return date; }
        public String getCoverUrl() { return coverUrl; }
    }

    private boolean en;                    // true on /en/... pages
    private List<Card> cards;              // list page: everything in this language, newest first

    // article page only
    private ArticlePackageModel article;
    private String heroImageUrl;           // site-relative, for <img>/<video poster>
    private String heroVideoUrl;           // null = no clip, the picture stays
    private String pageUrl;                // absolute, for canonical and og:url
    private String imageUrl;               // absolute hero picture, for og:image and JSON-LD
    private String zhAlternateUrl;         // absolute hreflang links; both null when the article has no counterpart
    private String enAlternateUrl;

    public BlogViewBinder() { }

    public boolean isEn() { return en; }
    public void setEn(boolean __en) { this.en = __en; }

    public List<Card> getCards() { return cards; }
    public void setCards(List<Card> __cards) { this.cards = __cards; }

    public ArticlePackageModel getArticle() { return article; }
    public void setArticle(ArticlePackageModel __article) { this.article = __article; }

    public String getHeroImageUrl() { return heroImageUrl; }
    public void setHeroImageUrl(String __url) { this.heroImageUrl = __url; }

    public String getHeroVideoUrl() { return heroVideoUrl; }
    public void setHeroVideoUrl(String __url) { this.heroVideoUrl = __url; }

    public String getPageUrl() { return pageUrl; }
    public void setPageUrl(String __url) { this.pageUrl = __url; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String __url) { this.imageUrl = __url; }

    public String getZhAlternateUrl() { return zhAlternateUrl; }
    public void setZhAlternateUrl(String __url) { this.zhAlternateUrl = __url; }

    public String getEnAlternateUrl() { return enAlternateUrl; }
    public void setEnAlternateUrl(String __url) { this.enAlternateUrl = __url; }

    /** True when the article is paired with one in the other language: hreflang links go in the head. */
    public boolean hasCounterpart() { return zhAlternateUrl != null; }
}
