package com.qilinfish.website.model;

/**
 * Pure data Model — a successful case study entry.
 * Currently a structural placeholder; cases will be populated later.
 */
public class CaseModel {

    private String id;
    private String title;
    private String client;
    private String summary;
    private String year;
    private String[] tags;

    public CaseModel() { }

    public CaseModel(String __id,
                     String __title,
                     String __client,
                     String __summary,
                     String __year,
                     String[] __tags) {
        this.id = __id;
        this.title = __title;
        this.client = __client;
        this.summary = __summary;
        this.year = __year;
        this.tags = __tags;
    }

    public String getId() { return id; }
    public void setId(String __id) { this.id = __id; }

    public String getTitle() { return title; }
    public void setTitle(String __title) { this.title = __title; }

    public String getClient() { return client; }
    public void setClient(String __client) { this.client = __client; }

    public String getSummary() { return summary; }
    public void setSummary(String __summary) { this.summary = __summary; }

    public String getYear() { return year; }
    public void setYear(String __year) { this.year = __year; }

    public String[] getTags() { return tags; }
    public void setTags(String[] __tags) { this.tags = __tags; }
}
