package com.qilinfish.website.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

/** The products entry page /products (spec B1–B6). */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
    "qilinfish.articles.dir=src/test/resources/articles",
    "qilinfish.survey.db-path=target/test-survey.db",
    "qilinfish.mail.enabled=false"
})
class ProductsIndexControllerTest {

    @Autowired
    TestRestTemplate http;

    @Test
    void twoStillCardsToTheTwoProductPagesAndTheContactLine() {
        String zh = ok("/products");
        assertThat(zh).contains("<h1 class=\"sc-title\">交出去之前，先確定它站得住</h1>",
            "<title>交出去之前，先確定它站得住 — 麒麟魚 Qilinfish Studio</title>");
        assertThat(zh).containsSubsequence("href=\"/products/verify\"", "src=\"/media/report_clean.jpg\"", "AI 說「做完了」", "交出去之前，找出錯在哪裡",
            "href=\"/products/content\"", "src=\"/media/content_clean.jpg\"", "每個事實都附原文，也附上它的限度", "做的時候就留下證據，交出去不怕被查",
            "也接 Web 開發與專案設計 →", "<a href=\"/contact\">聯絡我們</a>");
        assertThat(zh.split("class=\"pd-scene pe-card\"", -1)).hasSize(3);
        assertThat(zh).doesNotContain("<video", "治療", "預防", "……");
        String en = ok("/en/products");
        assertThat(en).contains("Before you hand it over, make sure it holds up", "href=\"/en/products/verify\"",
            "href=\"/en/products/content\"", "<a href=\"/en/contact\">Contact us</a>");
        assertThat(en.toLowerCase()).doesNotContain("<video", "cure", "treat", "prevent");
    }

    @Test
    void verificationPageStaysAtItsNewAddress() {
        assertThat(ok("/products/verify")).contains("data-pd-lens", "href=\"/survey/verify?pick=verify\"");
        assertThat(ok("/products")).doesNotContain("data-pd-lens");
    }

    @Test
    void aiLabelsReadTheSameSiteWide() {
        // D63: both cards are AI-made pictures and say so, the content card's picture included (CP10, D56)
        String zh = ok("/products");
        assertThat(zh.split("<span class=\"bl-ai\">示意畫面 · AI 生成</span>", -1)).hasSize(3);
        assertThat(zh).containsPattern("(?s)src=\"/media/content_clean\\.jpg\"[^>]*>\\s*<span class=\"bl-ai\">示意畫面 · AI 生成</span>");
        assertThat(zh.toLowerCase()).doesNotContain("placeholder");
        String en = ok("/en/products");
        assertThat(en.split("<span class=\"bl-ai\">Illustration · AI generated</span>", -1)).hasSize(3);
        assertThat(en).containsPattern("(?s)src=\"/media/content_clean\\.jpg\"[^>]*>\\s*<span class=\"bl-ai\">Illustration · AI generated</span>");
        assertThat(en.toLowerCase()).doesNotContain("placeholder");
        // D64: one English wording everywhere
        for (String path : new String[] { "/en/products", "/en/products/verify", "/en/about", "/en/survey/verify" }) {
            assertThat(ok(path)).as(path).contains("Illustration · AI generated").doesNotContain("Illustrative · AI-generated");
        }
    }

    @Test
    void noUntranslatedKey() {
        assertThat(ok("/products")).doesNotContainPattern("[#>\"\\s]pe\\.[a-z]");
        assertThat(ok("/en/products")).doesNotContainPattern("[#>\"\\s]pe\\.[a-z]");
    }

    private String ok(String __path) {
        ResponseEntity<String> r = http.getForEntity(__path, String.class);
        assertThat(r.getStatusCode()).as(__path).isEqualTo(HttpStatus.OK);
        return r.getBody();
    }
}
