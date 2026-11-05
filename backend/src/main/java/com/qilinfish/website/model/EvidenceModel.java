package com.qilinfish.website.model;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Pure data Model — one piece of evidence from an article package's evidence.json (CONTEXT.md, 證據):
 * {"evidence":[{"id":"E10", ...}]}. Written by CompanyofOne's export; the format is in
 * design/features/article-evidence/tickets.md. Every text is the fact-checker's original string.
 */
public class EvidenceModel {

    /** The file: {"evidence":[...]}, in E-number order. */
    public static class File {
        private List<EvidenceModel> evidence;

        public List<EvidenceModel> getEvidence() { return evidence; }
        public void setEvidence(List<EvidenceModel> __evidence) { this.evidence = __evidence; }
    }

    /** One line of the original quote; a translation line keeps its "中文：" prefix in the file (F4). */
    public static class Line {
        public static final String TRANSLATION_PREFIX = "中文：";

        private String text;
        private boolean translation;

        public String getText() { return text; }
        public void setText(String __text) { this.text = __text; }

        public boolean isTranslation() { return translation; }
        public void setTranslation(boolean __translation) { this.translation = __translation; }

        /** What the panel shows: a translation line without its "中文：" prefix (F4); any other line as it is (D3). */
        public String getShown() {
            if (translation && text != null && text.startsWith(TRANSLATION_PREFIX)) {
                return text.substring(TRANSLATION_PREFIX.length()).strip();
            }
            return text;
        }
    }

    private String id;          // E number, e.g. E10
    private String publisher;   // null when the fact-checker left it out (F3)
    private String title;
    private String date;        // as written, not always yyyy-MM-dd
    private List<Line> quote;   // required: at least one line
    private String note;        // optional, never shown (F2)
    private String qualifiers;  // required
    private String url;         // required: the URL field as written, at least one http(s) URL

    public String getId() { return id; }
    public void setId(String __id) { this.id = __id; }

    public String getPublisher() { return publisher; }
    public void setPublisher(String __publisher) { this.publisher = __publisher; }

    public String getTitle() { return title; }
    public void setTitle(String __title) { this.title = __title; }

    public String getDate() { return date; }
    public void setDate(String __date) { this.date = __date; }

    public List<Line> getQuote() { return quote; }
    public void setQuote(List<Line> __quote) { this.quote = __quote; }

    public String getNote() { return note; }
    public void setNote(String __note) { this.note = __note; }

    public String getQualifiers() { return qualifiers; }
    public void setQualifiers(String __qualifiers) { this.qualifiers = __qualifiers; }

    public String getUrl() { return url; }
    public void setUrl(String __url) { this.url = __url; }

    /** One piece of the URL field: a link (opens in a new tab) or the words around it. */
    public record UrlPart(String text, boolean link) { }

    private static final Pattern URL = Pattern.compile("https?://[^\\s<>\"（）()，。；、]+");

    /** The URL field as written, cut into links and plain words (it can hold several URLs and notes). */
    public List<UrlPart> getUrlParts() {
        List<UrlPart> out = new ArrayList<>();
        if (url == null) return out;
        Matcher m = URL.matcher(url);
        int at = 0;
        while (m.find()) {
            String link = m.group();
            // a sentence's full stop or comma right after a URL is not part of it
            while (!link.isEmpty() && ".,;:".indexOf(link.charAt(link.length() - 1)) >= 0) link = link.substring(0, link.length() - 1);
            if (m.start() > at) out.add(new UrlPart(url.substring(at, m.start()), false));
            out.add(new UrlPart(link, true));
            at = m.start() + link.length();
        }
        if (at < url.length()) out.add(new UrlPart(url.substring(at), false));
        return out;
    }

    /** Has at least one http(s) URL (a required field, tickets.md). */
    public boolean hasUrl() { return url != null && URL.matcher(url).find(); }
}
