package com.qilinfish.website.repository;

import com.qilinfish.website.model.ArticlePackageModel;
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
