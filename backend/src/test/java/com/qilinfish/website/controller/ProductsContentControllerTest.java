package com.qilinfish.website.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

/** The article-app product page /products/content (spec B8–B15). */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
    "qilinfish.articles.dir=src/test/resources/articles",
    "qilinfish.demo.zh=test-zh-solo:E1",
    "qilinfish.demo.en=test-en-image:E1",
    "qilinfish.survey.db-path=target/test-survey.db",
    "qilinfish.mail.enabled=false"
})
class ProductsContentControllerTest {

    @Autowired
    TestRestTemplate http;

    @Test
    void bothLanguagesHaveTheTitleAndTheButtonToTheirSurvey() {
        String zh = ok("/products/content");
        assertThat(zh).contains("<h1 class=\"sc-title\">每一句都查得到出處</h1>", "<title>每一句都查得到出處 — 麒麟魚 Qilinfish Studio</title>",
            "href=\"/survey/content\"");
        String en = ok("/en/products/content");
        assertThat(en).contains("<h1 class=\"sc-title\">Every sentence, traceable to its source</h1>", "href=\"/en/survey/content\"",
            "<html lang=\"en\"");
    }

    @Test
    void fivePartsInOrderWithTheExcerptAndBothRowsOfSteps() {
        String zh = ok("/products/content");
        assertThat(zh).containsSubsequence("每一句都查得到出處</h1>", "這篇沒有對應的英文文章。", "[1]",
            "為什麼平常做不到", ">查資料<", ">寫稿<", ">配圖<", ">排版<", ">上線<", "出處連結就斷一次",
            ">提問<", ">研究<", ">查核<", ">大綱<", ">製作<", ">驗收<", ">成品<",
            "實際案例", "怎麼付費",
            "一次買斷，永久使用；之後可選擇加購維護合約，取得更新與修正。沒有維護合約也能繼續用，只是拿不到更新。",
            "維護合約不含一對一技術支援。", "沒有維護合約，就拿不到任何更新，包括外部服務（AI API 等）改版造成的修正。",
            "這個產品還在打磨，還沒開放使用", "href=\"/survey/content\"");
        assertThat(zh).doesNotContain("pd-scenes", "class=\"pd-scene\"");   // no scenario cards
        String en = ok("/en/products/content");
        assertThat(en).containsSubsequence("Every sentence, traceable to its source</h1>",
            "This paragraph is test content for checking paragraphs, line height and citation styles.", "[1]", "Why it usually can");
    }

    @Test
    void noPriceAnywhereAndNoUntranslatedKey() {
        for (String path : new String[] { "/products/content", "/en/products/content" }) {
            String page = ok(path);
            assertThat(page).as(path).doesNotContain("US$", "$150", "$30", "150 美元", "30 美元");
            assertThat(page).as(path).doesNotContainPattern("[#>\"\\s]pc\\.[a-z]");
        }
    }

    @Test
    void theDemoIsARealParagraphWithItsEvidenceFromThePackage() {
        // article-evidence B14–B17: the paragraph and its evidence come from the package (qilinfish.demo); same panel as the blog
        String zh = ok("/products/content");
        assertThat(zh).contains("<div class=\"pc-demo-wrap\" data-ev-marks=\"這段話的出處\">",
            "<div class=\"pc-demo__hint\">點編號，看這段話的出處</div>",
            // F7: without JavaScript the [n] goes to the article page's reference list
            "<p>這篇沒有對應的英文文章。<sup class=\"cite\" data-e=\"E1\"><a href=\"/blog/test-zh-solo#r1\">[1]</a></sup></p>",
            "/js/qilinfish.js?v=20261105-evidence");
        assertThat(zh).containsSubsequence("<template data-ev-src=\"E1\">", "<b>測試出版者</b>", ">原文引句<",
            "<p class=\"ev-tr\">這是測試用的引句。</p>", ">限定條件<", "測試用的限定條件：只代表測試資料。", ">原始網址<",
            "<a href=\"https://example.com/report.pdf\" rel=\"noopener\" target=\"_blank\">", "</template>");
        assertThat(zh).contains("<meta name=\"description\" content=\"把一個主題做成每一句都查得到出處的內容：點文章裡的編號，就看得到原文引句、限定條件和原始網址。\">");
        // the old hand-written demo is gone
        assertThat(zh).doesNotContain("Ipsos", "pc-sent", "pc-ev", "data-ev=", "點一句");
        String en = ok("/en/products/content");
        assertThat(en).contains("data-ev-marks=\"Sources for this passage\"",
            "<div class=\"pc-demo__hint\">Click a number to see the source behind the passage</div>",
            "<a href=\"/en/blog/test-en-image#r1\">[1]</a>", ">Quote<", ">Qualifiers<", ">Source URL<",
            "click a number in an article to see the original quote");
        assertThat(en).doesNotContain("Stanford", "click a sentence", "中文：");
    }

    @Test
    void realExamplesAreTheArticlesWithEvidenceMarksInThePageLanguage() {
        String zh = ok("/products/content");
        assertThat(zh).containsSubsequence("實際案例", "<a class=\"bl-card\" href=\"/blog/test-zh-solo\">",
            "src=\"/articles/test-zh-solo/media/", "2026-", "怎麼付費");
        // test-zh-video has <sup class="cite"> without data-e; the English article is not on the Chinese page
        assertThat(zh).doesNotContain("/blog/test-zh-video", "test-en-image");
        String en = ok("/en/products/content");
        assertThat(en).containsSubsequence("Real examples", "<a class=\"bl-card\" href=\"/en/blog/test-en-image\">", "How you pay");
        assertThat(en).doesNotContain("test-zh-solo", "test-zh-video");
    }

    private String ok(String __path) {
        ResponseEntity<String> r = http.getForEntity(__path, String.class);
        assertThat(r.getStatusCode()).as(__path).isEqualTo(HttpStatus.OK);
        return r.getBody();
    }
}
