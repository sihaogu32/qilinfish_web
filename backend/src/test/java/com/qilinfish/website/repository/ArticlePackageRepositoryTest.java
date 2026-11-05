package com.qilinfish.website.repository;

import com.qilinfish.website.model.ArticlePackageModel;
import com.qilinfish.website.model.EvidenceModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Start-up checks on article packages (spec behaviour 6, the per-package part). Fixtures: src/test/resources/articles. */
class ArticlePackageRepositoryTest {

    static final Path FIXTURES = Path.of("src/test/resources/articles");

    @TempDir
    Path dir;

    @BeforeEach
    void copyFixtures() throws IOException {
        try (Stream<Path> all = Files.walk(FIXTURES)) {
            for (Path p : all.toList()) {
                Path to = dir.resolve(FIXTURES.relativize(p).toString());
                if (Files.isDirectory(p)) Files.createDirectories(to);
                else Files.copy(p, to, StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }

    @Test
    void loadsBothFixturesNewestFirstAndPointsMediaAtThePublicPath() throws IOException {
        ArticlePackageRepository repo = new ArticlePackageRepository(dir.toString());

        ArticlePackageModel zh = repo.findBySlug("test-zh-video").orElseThrow();
        assertThat(zh.isEn()).isFalse();
        assertThat(zh.getHero().getVideo()).isEqualTo("media/hero.mp4");
        String body = zh.getBodyBeforeCta() + zh.getBodyAfterCta();
        assertThat(body).contains("src=\"/articles/test-zh-video/media/fig-1.jpg\"").doesNotContain("\"media/");
        assertThat(repo.findBySlug("test-en-image").orElseThrow().getHero().getVideo()).isNull();
        assertThat(repo.findByLang(false)).extracting(ArticlePackageModel::getSlug).containsExactly("test-zh-video", "test-zh-solo");
        assertThat(repo.findByLang(true)).extracting(ArticlePackageModel::getSlug).containsExactly("test-en-image");
    }

    @Test
    void onlyACiteWithDataEIsAnEvidenceMark() throws IOException {
        ArticlePackageRepository repo = new ArticlePackageRepository(dir.toString());
        // test-zh-video has <sup class="cite"> without data-e: a plain reference number, not an evidence mark
        assertThat(repo.findBySlug("test-zh-video").orElseThrow().isEvidenceMarked()).isFalse();
        assertThat(repo.findBySlug("test-zh-solo").orElseThrow().isEvidenceMarked()).isTrue();
        assertThat(repo.findBySlug("test-en-image").orElseThrow().isEvidenceMarked()).isTrue();
        assertThat(repo.findEvidenceMarkedByLang(false)).extracting(ArticlePackageModel::getSlug).containsExactly("test-zh-solo");
        assertThat(repo.findEvidenceMarkedByLang(true)).extracting(ArticlePackageModel::getSlug).containsExactly("test-en-image");
    }

    @Test
    void evidenceMarkedIsNeverTakenFromArticleJson() throws IOException {
        Path json = dir.resolve("test-zh-video/article.json");
        String s = Files.readString(json, StandardCharsets.UTF_8);
        Files.writeString(json, s.replaceFirst("\\{","{ \"evidenceMarked\" : true,"), StandardCharsets.UTF_8);
        assertThat(new ArticlePackageRepository(dir.toString()).findBySlug("test-zh-video").orElseThrow().isEvidenceMarked()).isFalse();
    }

    // ---- article-evidence B6–B8: evidence.json ----

    @Test
    void evidenceIsLoadedForAMarkedArticle() throws IOException {
        ArticlePackageRepository repo = new ArticlePackageRepository(dir.toString());
        ArticlePackageModel zh = repo.findBySlug("test-zh-solo").orElseThrow();
        assertThat(zh.getEvidence()).containsOnlyKeys("E1");
        EvidenceModel e1 = zh.getEvidence().get("E1");
        assertThat(e1.getPublisher()).isEqualTo("測試出版者");
        assertThat(e1.getQuote()).extracting(EvidenceModel.Line::isTranslation).containsExactly(false, true);
        // the file keeps "中文：" (F4); the panel shows the line without it
        assertThat(e1.getQuote().get(1).getText()).isEqualTo("中文：這是測試用的引句。");
        assertThat(e1.getQuote().get(1).getShown()).isEqualTo("這是測試用的引句。");
        assertThat(e1.getQuote().get(0).getShown()).isEqualTo("\"This is a test quote.\"");
        assertThat(e1.getUrlParts()).containsExactly(new EvidenceModel.UrlPart("https://example.com/report.pdf", true),
            new EvidenceModel.UrlPart("（第 3 頁）", false));
        EvidenceModel en = repo.findBySlug("test-en-image").orElseThrow().getEvidence().get("E1");
        assertThat(en.getUrlParts()).extracting(EvidenceModel.UrlPart::text)
            .containsExactly("https://example.com/report.pdf", "; news page: ", "https://example.com/news", ".");
    }

    @Test
    void articleWithoutMarksNeedsNoEvidenceFile() throws IOException {
        // test-zh-video has no data-e and no evidence.json; a broken file there is not even read
        Files.writeString(dir.resolve("test-zh-video/evidence.json"), "{ broken", StandardCharsets.UTF_8);
        ArticlePackageModel zh = new ArticlePackageRepository(dir.toString()).findBySlug("test-zh-video").orElseThrow();
        assertThat(zh.getEvidence()).isEmpty();
    }

    @Test
    void markedArticleWithoutEvidenceFileStopsStartUp() throws IOException {
        Files.delete(dir.resolve("test-zh-solo/evidence.json"));
        fails("test-zh-solo: body.html has evidence marks but evidence.json not found");
    }

    @Test
    void everyENumberOfEveryMarkMustBeInTheFile() throws IOException {
        body("test-en-image", "<p>More.<sup class=\"cite\" data-e=\"E1 E7  E9\"><a href=\"#r1\">[1]</a></sup></p>");
        fails("test-en-image: evidence marks point at E7, E9, not in evidence.json");
    }

    @Test
    void quoteQualifiersAndUrlAreRequired() throws IOException {
        evidence("test-zh-solo", "\"qualifiers\":\"測試用的限定條件：只代表測試資料。\"", "\"qualifiers\":\" \"");
        evidence("test-en-image", "\"url\":\"https://example.com/report.pdf; news page: https://example.com/news.\"", "\"url\":\"see the report\"");
        fails("test-zh-solo: evidence E1 in evidence.json has no qualifiers", "test-en-image: evidence E1 in evidence.json has no url");
        evidence("test-zh-solo", "\"quote\":[", "\"x\":1,\"quote\":[");
        fails("test-zh-solo: evidence.json has an unknown field 'x'");
        evidence("test-zh-solo", "\"x\":1,\"quote\":[", "\"quote\":[");
        Path f = dir.resolve("test-zh-solo/evidence.json");
        Files.writeString(f, Files.readString(f, StandardCharsets.UTF_8).replaceAll("(?s)\"quote\":\\[.*?\\],\\s*\"note\"", "\"quote\":[],\"note\""),
            StandardCharsets.UTF_8);
        fails("test-zh-solo: evidence E1 in evidence.json has no quote, no qualifiers");
    }

    @Test
    void badEvidenceJson() throws IOException {
        Files.writeString(dir.resolve("test-zh-solo/evidence.json"), "{ \"evidence\": [", StandardCharsets.UTF_8);
        fails("test-zh-solo: evidence.json is not valid");
    }

    @Test
    void onlyTheEvidenceTheMarksUseIsKept() throws IOException {
        Path f = dir.resolve("test-zh-solo/evidence.json");
        String s = Files.readString(f, StandardCharsets.UTF_8).stripTrailing();
        Files.writeString(f, s.substring(0, s.length() - 2) + ",{\"id\":\"E99\",\"publisher\":null,\"title\":null,\"date\":null,"
            + "\"quote\":[],\"note\":null,\"qualifiers\":null,\"url\":null}]}", StandardCharsets.UTF_8);
        // E99 is not used by any mark: not checked and not kept
        assertThat(new ArticlePackageRepository(dir.toString()).findBySlug("test-zh-solo").orElseThrow().getEvidence())
            .containsOnlyKeys("E1");
    }

    @Test
    void bodyIsSplitRightBeforeTheInviteBoxEndTag() throws IOException {
        ArticlePackageModel zh = new ArticlePackageRepository(dir.toString()).findBySlug("test-zh-video").orElseThrow();
        String original = Files.readString(dir.resolve("test-zh-video/body.html"), StandardCharsets.UTF_8)
            .replace("src=\"media/", "src=\"/articles/test-zh-video/media/");
        // nothing re-serialised: the two halves put back together are the original markup
        assertThat(zh.getBodyBeforeCta() + zh.getBodyAfterCta()).isEqualTo(original);
        assertThat(zh.getBodyBeforeCta().stripTrailing()).endsWith("<p>您遇過這種情況嗎？這是測試用的提問。</p>");
        assertThat(zh.getBodyAfterCta()).startsWith("</div>");
        assertThat(zh.getBodyAfterCta().substring("</div>".length()).stripLeading()).startsWith("<h2 id=\"refs\">");
    }

    @Test
    void exactlyOneInviteBox() throws IOException {
        Path zh = dir.resolve("test-zh-video/body.html");
        String s = Files.readString(zh, StandardCharsets.UTF_8);
        Files.writeString(zh, s.replace("class=\"pd-stage bl-invite\"", "class=\"pd-stage\""), StandardCharsets.UTF_8);
        body("test-en-image", "<div class=\"bl-invite\"><p>again</p></div>");
        fails("test-zh-video: body.html must have exactly one .bl-invite box, found 0",
              "test-en-image: body.html must have exactly one .bl-invite box, found 2");
    }

    @Test
    void inviteBoxHasNoLinks() throws IOException {
        Path f = dir.resolve("test-zh-solo/body.html");
        String s = Files.readString(f, StandardCharsets.UTF_8);
        Files.writeString(f, s.replace("<p>這篇也有提問框。</p>", "<p>這篇也有提問框。</p><a href=\"/survey\">填寫問卷</a>"), StandardCharsets.UTF_8);
        fails("test-zh-solo: the .bl-invite box in body.html must not contain links");
    }

    @Test
    void emptyFolderMeansNoArticles() throws IOException {
        Path empty = Files.createDirectory(dir.resolve("empty"));
        assertThat(new ArticlePackageRepository(empty.toString()).findByLang(false)).isEmpty();
    }

    @Test
    void missingFolderStopsStartUp() {
        assertThatThrownBy(() -> new ArticlePackageRepository(dir.resolve("nope").toString()))
            .hasMessageContaining("is not a folder");
    }

    @Test
    void slugMustMatchFolder() throws IOException {
        json("test-zh-video", "\"slug\": \"test-zh-video\"", "\"slug\": \"other\"");
        fails("test-zh-video: slug in article.json is 'other'");
    }

    @Test
    void langMustBeZhOrEn() throws IOException {
        json("test-en-image", "\"lang\": \"en\"", "\"lang\": \"fr\"");
        fails("test-en-image: lang is 'fr', must be zh or en");
    }

    @Test
    void requiredFields() throws IOException {
        json("test-zh-video", "\"lede\":", "\"ledeX\":");
        fails("test-zh-video: article.json has an unknown field 'ledeX'");
        json("test-zh-video", "\"ledeX\":", "\"lede\":");
        json("test-zh-video", "\"title\": \"測試文章：有頁首影片的中文文章\"", "\"title\": \" \"");
        fails("test-zh-video: title is missing");
    }

    @Test
    void heroImageIsRequiredVideoIsNot() throws IOException {
        json("test-en-image", "\"image\": \"media/hero.jpg\"", "\"image\": null");
        fails("test-en-image: hero.image is missing");
    }

    @Test
    void referencedMediaMustExist() throws IOException {
        Files.delete(dir.resolve("test-zh-video/media/hero.mp4"));
        Files.delete(dir.resolve("test-en-image/media/fig-1.jpg"));
        fails("test-zh-video: hero.video 'media/hero.mp4' not found", "test-en-image: body.html 'media/fig-1.jpg' not found");
    }

    @Test
    void mediaMustStayInsideMedia() throws IOException {
        json("test-en-image", "\"cover\": \"media/hero.jpg\"", "\"cover\": \"media/../article.json\"");
        fails("test-en-image: cover 'media/../article.json' must be a file under media/");
    }

    @Test
    void bodyHoldsArticleContentOnly() throws IOException {
        body("test-zh-video", "<style>.x{}</style>");
        body("test-en-image", "<HEAD>");
        fails("test-zh-video: body.html contains <style>", "test-en-image: body.html contains <head>");
        body("test-zh-video", "<html lang=\"zh\">");
        fails("test-zh-video: body.html contains <html>");
    }

    @Test
    void headerTagIsNotHead() throws IOException {
        body("test-zh-video", "<header class=\"x\"></header>");
        new ArticlePackageRepository(dir.toString());
    }

    @Test
    void dateMustBeIsoDay() throws IOException {
        json("test-zh-video", "\"date\": \"2026-11-02\"", "\"date\": \"2026/11/02\"");
        fails("test-zh-video: date '2026/11/02' must be yyyy-MM-dd");
    }

    @Test
    void missingFilesAndBadJson() throws IOException {
        Files.delete(dir.resolve("test-zh-video/body.html"));
        Files.writeString(dir.resolve("test-en-image/article.json"), "{ \"slug\": ", StandardCharsets.UTF_8);
        fails("test-zh-video: body.html not found", "test-en-image: article.json is not valid");
    }

    private void json(String __slug, String __from, String __to) throws IOException {
        Path f = dir.resolve(__slug + "/article.json");
        String s = Files.readString(f, StandardCharsets.UTF_8);
        assertThat(s).contains(__from);
        Files.writeString(f, s.replace(__from, __to), StandardCharsets.UTF_8);
    }

    private void evidence(String __slug, String __from, String __to) throws IOException {
        Path f = dir.resolve(__slug + "/evidence.json");
        String s = Files.readString(f, StandardCharsets.UTF_8);
        assertThat(s).contains(__from);
        Files.writeString(f, s.replace(__from, __to), StandardCharsets.UTF_8);
    }

    private void body(String __slug, String __extra) throws IOException {
        Path f = dir.resolve(__slug + "/body.html");
        Files.writeString(f, Files.readString(f, StandardCharsets.UTF_8) + __extra, StandardCharsets.UTF_8);
    }

    private void fails(String... __messages) {
        assertThatThrownBy(() -> new ArticlePackageRepository(dir.toString()))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Broken article packages")
            .hasMessageContainingAll(__messages);
    }
}
