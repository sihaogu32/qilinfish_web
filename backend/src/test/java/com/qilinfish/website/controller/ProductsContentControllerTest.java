package com.qilinfish.website.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.HtmlUtils;

import static org.assertj.core.api.Assertions.assertThat;

/** The article-app product page /products/content (spec B8–B15). */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
    "qilinfish.articles.dir=src/test/resources/articles",
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
        assertThat(zh).containsSubsequence("每一句都查得到出處</h1>",
            "Ipsos 2025 年在 30 個國家訪問了 23,216 人，平均 79% 同意「用 AI 的產品與服務應該要揭露這件事」。", "[3]",
            "Salesforce 2024 年調查 18 國的 15,015 名消費者，近 75% 想知道自己是不是在跟 AI 代理溝通。", "[4]",
            "為什麼平常做不到", ">查資料<", ">寫稿<", ">配圖<", ">排版<", ">上線<", "出處連結就斷一次",
            ">提問<", ">研究<", ">查核<", ">大綱<", ">製作<", ">驗收<", ">成品<",
            "實際案例", "怎麼付費",
            "一次買斷，永久使用；之後可選擇加購維護合約，取得更新與修正。沒有維護合約也能繼續用，只是拿不到更新。",
            "維護合約不含一對一技術支援。", "沒有維護合約，就拿不到任何更新，包括外部服務（AI API 等）改版造成的修正。",
            "這個產品還在打磨，還沒開放使用", "href=\"/survey/content\"");
        assertThat(zh).doesNotContain("pd-scenes", "class=\"pd-scene\"");   // no scenario cards
        String en = ok("/en/products/content");
        assertThat(en).containsSubsequence("A Stanford-hosted study gives a bounded example of manual checking in legal work.",
            "Researchers built a preregistered dataset of more than 200 legal queries and manually reviewed outputs from three tested systems for accuracy and fidelity to legal authority.",
            "The query count describes that research dataset, not all legal questions or systems.", "[10]");
        assertThat(en).doesNotContain("Ipsos", "[3]");
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
    void eachSentenceOpensItsEvidenceWordForWord() {
        String zh = HtmlUtils.htmlUnescape(ok("/products/content"));
        assertThat(zh).contains("data-ev=\"e10\"", "data-ev=\"e12\"", "tabindex=\"0\"",
            "Seventy-nine percent agree (40% strongly agree) that companies using AI should have to disclose that use.",
            "中文：79% 同意（40% 非常同意）用 AI 的公司應該要揭露這件事。",
            "How much do you agree or disagree with the following? Products and services using artificial intelligence should have to disclose that use",
            "中文：你多同意下列說法？用人工智慧的產品與服務應該要揭露這件事。",
            "30-country average 79% Indonesia 91% … Japan 66%", "不同意欄 30 國平均 7%。",
            "\"Base: 23,216 online adults under age 75 across 30 countries, interviewed March 21 – April 4, 2025\"。部分國家樣本比一般人口更都市、更高學歷或更富裕。30 國平均未依人口加權，不代表總體結果。題目是「產品與服務」，不是專業顧問服務。台灣不在 30 國內。",
            "href=\"https://www.ipsos.com/sites/default/files/ct/news/documents/2025-06/Ipsos-AI-Monitor-2025_0.pdf\"",
            "Nearly 75% of consumers want to know if they're communicating with an AI agent.",
            "中文：近 75% 的消費者想知道自己是不是在跟 AI 代理溝通。",
            "15,015 名消費者，2024-07-16 至 08-16，YouGov 線上執行；加權後代表 18 國成人（18 歲以上）。同頁樣本數寫法不一致，引用時用 15,015。出版者販售 AI 代理產品。題目是「跟 AI 代理溝通」，不是「交付成果用了 AI」。台灣不在 18 國內。",
            "href=\"https://www.salesforce.com/news/stories/ai-customer-research/\"");
        // a quote and its qualifiers sit in the same panel
        assertThat(zh).containsPattern("(?s)id=\"pc-ev-e10\".*?Seventy-nine.*?Base: 23,216.*?</aside>");
        String en = HtmlUtils.htmlUnescape(ok("/en/products/content"));
        assertThat(en).contains("Stanford Law School hosts the paper; the authors are affiliated with Stanford University and Yale University.",
            "Hallucination-Free? Assessing the Reliability of Leading AI Legal Research Tools", ">2024-05-30<",
            "Second, we manually construct a preregistered dataset of over 200 legal queries for identifying and understanding vulnerabilities in legal AI tools. We run these queries on LexisNexis, Thomson Reuters, and GPT-4 and manually review their outputs for accuracy and fidelity to authority.",
            "The “over 200” figure describes legal queries in the researchers’ preregistered dataset, not people or all legal queries. The tested systems were LexisNexis, Thomson Reuters, and GPT-4. The findings concern those tools and queries.",
            "the PDF labels the manuscript “Preprint. Under review.”",
            "href=\"https://law.stanford.edu/wp-content/uploads/2024/05/Legal_RAG_Hallucinations.pdf\"");
        assertThat(en).doesNotContain("中文：", "Salesforce", "pc-ev-e10");
        assertThat(en.split("data-ev=\"e12\"", -1)).hasSize(4);   // all three sentences open E12
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
