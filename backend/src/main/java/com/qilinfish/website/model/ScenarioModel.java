package com.qilinfish.website.model;

/**
 * Pure data Model — one "have you been here?" scenario card on the products page.
 * No view-state, no formatting; that lives in the corresponding ViewBinder.
 */
public class ScenarioModel {

    private String title;
    private String pick;       // survey first-step choice: verify / explain / inventory
    private String media;      // file stem under /media: <media>.mp4 + <media>.jpg (poster)

    public ScenarioModel() { }

    public ScenarioModel(String __title,
                         String __pick,
                         String __media) {
        this.title = __title;
        this.pick = __pick;
        this.media = __media;
    }

    public String getTitle() { return title; }
    public void setTitle(String __title) { this.title = __title; }

    public String getPick() { return pick; }
    public void setPick(String __pick) { this.pick = __pick; }

    public String getMedia() { return media; }
    public void setMedia(String __media) { this.media = __media; }
}
