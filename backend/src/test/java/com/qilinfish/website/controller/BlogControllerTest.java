package com.qilinfish.website.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Blog pages over a real server (the /en prefix is a servlet forward, which MockMvc does not follow).
 * Article packages come from the test fixtures, never from content/.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
    "qilinfish.articles.dir=src/test/resources/articles",
    "qilinfish.survey.db-path=target/test-survey.db",
    "qilinfish.mail.enabled=false"
})
class BlogControllerTest {

    @Autowired
    TestRestTemplate http;

    @LocalServerPort
    int port;

    @Test
    void eachListShowsOnlyItsOwnLanguage() {
        String zh = ok("/blog");
        assertThat(zh).contains("href=\"/blog/test-zh-video\"", "href=\"/blog/test-zh-solo\"",
            "src=\"/articles/test-zh-video/media/hero.jpg\"").doesNotContain("test-en-image");
        String en = ok("/en/blog");
        assertThat(en).contains("href=\"/en/blog/test-en-image\"").doesNotContain("test-zh-video", "test-zh-solo");
    }

    @Test
    void theOtherLanguageUrlIsNotFound() {
        assertThat(status("/en/blog/test-zh-video")).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(status("/blog/test-en-image")).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(status("/blog/no-such-article")).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void articleWithClipPlaysItOverThePicture() {
        String page = ok("/blog/test-zh-video");
        assertThat(page).contains(
            "<video class=\"ab-hero__video\" src=\"/articles/test-zh-video/media/hero.mp4\" poster=\"/articles/test-zh-video/media/hero.jpg\"",
            "<h1 class=\"ab-hero__title\">測試文章：有頁首影片的中文文章</h1>",
            "<img src=\"/articles/test-zh-video/media/fig-1.jpg\"",
            "<meta property=\"og:image\" content=\"http://localhost:" + port + "/articles/test-zh-video/media/hero.jpg\">",
            "<link rel=\"canonical\" href=\"http://localhost:" + port + "/blog/test-zh-video\">",
            "<meta name=\"description\" content=\"網站測試用的中文文章包");
        assertThat(page).doesNotContain("<img class=\"ab-hero__video\"");
    }

    @Test
    void articleWithoutClipShowsThePicture() {
        String page = ok("/en/blog/test-en-image");
        assertThat(page).contains(
            "<img class=\"ab-hero__video\" src=\"/articles/test-en-image/media/hero.jpg\"",
            "<html lang=\"en\"",
            "<link rel=\"canonical\" href=\"http://localhost:" + port + "/en/blog/test-en-image\">",
            "\"inLanguage\": \"en\"");
        assertThat(page).doesNotContain("<video");
    }

    @Test
    void pairedArticlesPointAtEachOther() {
        String zh = ok("/blog/test-zh-video");
        assertThat(zh).contains(
            "<a data-lang=\"en\" href=\"/en/blog/test-en-image\"",
            "<link rel=\"alternate\" hreflang=\"zh-Hant-TW\" href=\"http://localhost:" + port + "/blog/test-zh-video\">",
            "<link rel=\"alternate\" hreflang=\"en\" href=\"http://localhost:" + port + "/en/blog/test-en-image\">");
        String en = ok("/en/blog/test-en-image");
        assertThat(en).contains(
            "<a data-lang=\"zh\" href=\"/blog/test-zh-video\"",
            "<link rel=\"alternate\" hreflang=\"zh-Hant-TW\" href=\"http://localhost:" + port + "/blog/test-zh-video\">",
            "<link rel=\"alternate\" hreflang=\"en\" href=\"http://localhost:" + port + "/en/blog/test-en-image\">");
    }

    @Test
    void unpairedArticleSwitchesToTheOtherList() {
        String page = ok("/blog/test-zh-solo");
        assertThat(page).contains("<a data-lang=\"en\" href=\"/en/blog\"", "<a data-lang=\"zh\" href=\"/blog/test-zh-solo\"")
            .doesNotContain("hreflang=\"en\" href=\"http", "rel=\"alternate\"");
    }

    @Test
    void surveyButtonGoesAtTheEndOfTheArticlesOwnInviteBox() {
        String zh = ok("/blog/test-zh-solo");
        assertThat(zh).containsOnlyOnce("bl-invite").doesNotContain("<aside");
        assertThat(zh).containsPattern("<p>這篇也有提問框。</p>\\s*<a class=\"qf-btn qf-btn--primary\" href=\"/survey/verify\">填寫問卷 <span class=\"arr\">→</span></a>\\s*</div>");
        String en = ok("/en/blog/test-en-image");
        assertThat(en).containsOnlyOnce("bl-invite");
        assertThat(en).containsPattern("<p>Have you run into this\\? This is a test question\\.</p>\\s*<a class=\"qf-btn qf-btn--primary\" href=\"/en/survey/verify\">Take the survey <span class=\"arr\">→</span></a>\\s*</div>\\s*<h2 id=\"refs\">");
    }

    @Test
    void articleWithEvidenceMarksEndsWithTheLineToTheProductPage() {
        String zh = ok("/blog/test-zh-solo");
        assertThat(zh).containsPattern("</div>\\s*<p class=\"bl-sourced\"><a href=\"/products/content\">這篇文章由麒麟魚的內容工具製作，每一句都查得到出處 <span class=\"arr\">→</span></a></p>\\s*</article>");
        String en = ok("/en/blog/test-en-image");
        assertThat(en).containsPattern("<p class=\"bl-sourced\"><a href=\"/en/products/content\">This article was made with Qilinfish&#39;s content tool, and every sentence can be traced to its source <span class=\"arr\">→</span></a></p>\\s*</article>");
        // after the references list, before the footer (D45)
        assertThat(en).containsSubsequence("<h2 id=\"refs\">", "class=\"bl-sourced\"", "</article>", "<footer");
        // <sup class="cite"> without data-e is not an evidence mark
        assertThat(ok("/blog/test-zh-video")).doesNotContain("bl-sourced", "/products/content");
    }

    @Test
    void oldEnglishShadowAiUrlMovedForGood() throws Exception {
        HttpResponse<Void> r = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NEVER).build().send(
            HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/en/blog/shadow-ai")).GET().build(),
            HttpResponse.BodyHandlers.discarding());
        assertThat(r.statusCode()).isEqualTo(301);
        assertThat(r.headers().firstValue("Location")).hasValueSatisfying(l -> assertThat(l).endsWith("/en/blog/shadow-ai-en"));
    }

    @Test
    void onlyMediaIsPublic() {
        assertThat(status("/articles/test-zh-video/media/hero.mp4")).isEqualTo(HttpStatus.OK);
        assertThat(status("/articles/test-zh-video/article.json")).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(status("/articles/test-zh-video/body.html")).isEqualTo(HttpStatus.NOT_FOUND);
        // each package's handler is rooted at its media/ folder, so climbing out of it finds nothing
        assertThat(status("/articles/test-zh-video/media/../article.json")).isNotEqualTo(HttpStatus.OK);
        assertThat(status("/articles/test-en-image/media/../../test-zh-video/body.html")).isNotEqualTo(HttpStatus.OK);
        assertThat(status("/articles/no-such-article/media/hero.jpg")).isEqualTo(HttpStatus.NOT_FOUND);
    }

    private String ok(String __path) {
        ResponseEntity<String> r = http.getForEntity(__path, String.class);
        assertThat(r.getStatusCode()).as(__path).isEqualTo(HttpStatus.OK);
        return r.getBody();
    }

    private HttpStatus status(String __path) {
        return HttpStatus.valueOf(http.getForEntity(__path, String.class).getStatusCode().value());
    }
}
