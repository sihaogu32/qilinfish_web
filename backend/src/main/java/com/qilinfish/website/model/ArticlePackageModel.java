package com.qilinfish.website.model;

/**
 * Pure data Model — one article package from CompanyofOne: content/articles/<slug>/article.json.
 * An article is written in one language only (zh or en); it is not a translation of another article.
 * Paths (hero.image, hero.video, cover) are relative to the package folder and always start with media/.
 * The body is the package's body.html, unchanged except media/ paths (pointed at /articles/<slug>/media/), split in two
 * right before the end tag of its .bl-invite box, where the site puts the survey button (filled by the repository).
 */
public class ArticlePackageModel {

    public static final String ZH = "zh";
    public static final String EN = "en";

    private String slug;       // same as the folder name and the URL part
    private String lang;       // "zh" or "en"
    private String title;
    private String lede;       // under the title in the page header
    private String summary;    // list card text, <meta name="description">, og:description
    private String date;       // yyyy-MM-dd
    private Hero hero;
    private String cover;      // list card picture, e.g. media/hero.jpg
    private String bodyBeforeCta;   // not in the JSON
    private String bodyAfterCta;    // not in the JSON; starts with the .bl-invite box's end tag

    /** Page header: the picture is required; the clip is optional and plays over it once loaded. */
    public static class Hero {
        private String image;
        private String video;

        public String getImage() { return image; }
        public void setImage(String __image) { this.image = __image; }

        public String getVideo() { return video; }
        public void setVideo(String __video) { this.video = __video; }
    }

    public boolean isEn() { return EN.equals(lang); }

    public String getSlug() { return slug; }
    public void setSlug(String __slug) { this.slug = __slug; }

    public String getLang() { return lang; }
    public void setLang(String __lang) { this.lang = __lang; }

    public String getTitle() { return title; }
    public void setTitle(String __title) { this.title = __title; }

    public String getLede() { return lede; }
    public void setLede(String __lede) { this.lede = __lede; }

    public String getSummary() { return summary; }
    public void setSummary(String __summary) { this.summary = __summary; }

    public String getDate() { return date; }
    public void setDate(String __date) { this.date = __date; }

    public Hero getHero() { return hero; }
    public void setHero(Hero __hero) { this.hero = __hero; }

    public String getCover() { return cover; }
    public void setCover(String __cover) { this.cover = __cover; }

    public String getBodyBeforeCta() { return bodyBeforeCta; }
    public void setBodyBeforeCta(String __html) { this.bodyBeforeCta = __html; }

    public String getBodyAfterCta() { return bodyAfterCta; }
    public void setBodyAfterCta(String __html) { this.bodyAfterCta = __html; }
}
