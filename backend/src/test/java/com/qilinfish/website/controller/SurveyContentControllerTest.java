package com.qilinfish.website.controller;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/** The article-app survey /survey/content (spec B16–B25): one row per submission in survey_content, never in survey. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
    "qilinfish.articles.dir=src/test/resources/articles",
    "qilinfish.survey.db-path=target/test-survey.db",
    "qilinfish.mail.enabled=false"
})
class SurveyContentControllerTest {

    @LocalServerPort
    int port;

    final HttpClient client = HttpClient.newHttpClient();

    @Test
    void bothPagesShowEveryQuestionWithNoUntranslatedKey() throws Exception {
        for (String path : new String[] { "/survey/content", "/en/survey/content" }) {
            String page = get(path).body();
            assertThat(page).as(path).doesNotContainPattern("[#>\"\\s]sc\\.[a-z]");
            assertThat(page).as(path).contains("action=\"" + (path.startsWith("/en") ? "/en" : "") + "/survey/content\"",
                "data-q=\"role\"", "data-q=\"teamSize\"", "data-q=\"monthlyPieces\"", "data-q=\"lastPiece\"",
                "data-q=\"howChecked\"", "data-q=\"checkHours\"", "data-q=\"afterDelivery\"", "data-q=\"afterDeliveryCost\"",
                "data-q=\"tools\"", "data-q=\"toolCount\"", "data-q=\"moveHours\"", "data-q=\"formats\"",
                "data-q=\"blind\"", "data-q=\"revealBuy\"", "data-q=\"revealRenew\"",
                "data-q=\"name\"", "data-q=\"contact\"", "data-q=\"trial\"",
                // choice values are the Chinese option text on both pages
                "value=\"研究型創作者（部落格、電子報、YouTube…）\"", "value=\"有人指出錯誤\"", "value=\"看當時的更新內容\"",
                "US$150", "US$30");
        }
        // one <head> each: the page with a description (fragments/head.html) and an old page (layout :: head)
        assertThat(get("/survey/content").body()).containsOnlyOnce("<head>");
        assertThat(get("/survey/verify").body()).containsOnlyOnce("<head>").doesNotContain("og:description");
        assertThat(get("/survey/content").body()).contains("<h1 class=\"sc-title\">您交出去的內容，出處都查得到嗎？</h1>",
            "<title>您交出去的內容，出處都查得到嗎？ — 麒麟魚 Qilinfish Studio</title>", "<meta name=\"description\"");
    }

    @Test
    void chineseAndEnglishAnswersAreEachOneRowWithChineseChoices() throws Exception {
        long survey = SurveyControllerTest.count("survey");
        long before = SurveyControllerTest.count("survey_content");

        Map<String, Object> zh = fullAnswers();
        zh.put("name", "中文填答");
        HttpResponse<String> r = post("/survey/content", zh);
        assertThat(r.body()).contains("qf-form-status ok");
        assertThat(lastRow()).containsExactly("zh", "研究型創作者（部落格、電子報、YouTube…）", "Claude、Perplexity、其他：Notion AI",
            "電子報、短影音、影片腳本", "US$120", "會考慮", "看當時的更新內容", "有人指出錯誤", "要重做一份", "1.5", "0", "中文填答");

        Map<String, Object> en = fullAnswers();
        en.put("name", "English answer");
        r = post("/en/survey/content", en);
        assertThat(r.body()).contains("qf-form-status ok");
        assertThat(lastRow()).containsExactly("en", "研究型創作者（部落格、電子報、YouTube…）", "Claude、Perplexity、其他：Notion AI",
            "電子報、短影音、影片腳本", "US$120", "會考慮", "看當時的更新內容", "有人指出錯誤", "要重做一份", "1.5", "0", "English answer");

        assertThat(SurveyControllerTest.count("survey_content")).isEqualTo(before + 2);
        assertThat(SurveyControllerTest.count("survey")).isEqualTo(survey);
    }

    @Test
    void openQuestionsCanAllBeLeftBlank() throws Exception {
        long before = SurveyControllerTest.count("survey_content");
        HttpResponse<String> r = post("/survey/content", requiredOnly());
        assertThat(r.body()).contains("qf-form-status ok");
        assertThat(SurveyControllerTest.count("survey_content")).isEqualTo(before + 1);
    }

    @Test
    void eachMissingRequiredAnswerIsPointedOut() throws Exception {
        String[][] cases = {
            { "role", "role" }, { "teamSize", "teamSize" }, { "monthlyPieces", "monthlyPieces" },
            { "blindUsd", "blind" }, { "revealBuy", "revealBuy" }, { "revealRenew", "revealRenew" },
            { "name", "name" }, { "contact", "contact" } };
        for (String[] c : cases) {
            long before = SurveyControllerTest.count("survey_content");
            Map<String, Object> answers = requiredOnly();
            answers.remove(c[0]);
            HttpResponse<String> r = post("/survey/content", answers);
            assertThat(r.body()).as(c[0]).contains("qf-form-status err", "class=\"qf-field is-invalid\" data-q=\"" + c[1] + "\"");
            assertThat(r.body().split("qf-field is-invalid", -1)).as(c[0] + ": only that question is marked").hasSize(2);
            assertThat(SurveyControllerTest.count("survey_content")).as(c[0]).isEqualTo(before);
        }
    }

    @Test
    void blindPriceMustBeANumberOrAnOption() throws Exception {
        long before = SurveyControllerTest.count("survey_content");
        Map<String, Object> answers = requiredOnly();
        answers.put("blindUsd", "一百");
        HttpResponse<String> r = post("/survey/content", answers);
        assertThat(r.body()).contains("class=\"qf-field is-invalid\" data-q=\"blind\"", "金額只能填數字（不加幣別），例如 500 或 1,200.5。");
        answers.put("blindUsd", "12345678901234567");
        assertThat(post("/survey/content", answers).body()).contains("金額最多 16 位數。");
        answers.remove("blindUsd");
        answers.put("blindOption", "不會付費");
        assertThat(post("/survey/content", answers).body()).contains("qf-form-status ok");
        assertThat(lastColumn("blind_price")).isEqualTo("不會付費");
        assertThat(SurveyControllerTest.count("survey_content")).isEqualTo(before + 1);
    }

    @Test
    void otherNeedsItsOwnWordsAndHoursMustBeNumbers() throws Exception {
        Map<String, Object> answers = requiredOnly();
        answers.put("role", "其他");
        assertThat(post("/survey/content", answers).body()).contains("class=\"qf-field is-invalid\" data-q=\"role\"");
        answers.put("roleOther", "大學講師");
        answers.put("checkHours", "兩小時");
        assertThat(post("/survey/content", answers).body()).contains("class=\"qf-field is-invalid\" data-q=\"checkHours\"");
        answers.remove("checkHours");
        assertThat(post("/survey/content", answers).body()).contains("qf-form-status ok");
        assertThat(lastColumn("role")).isEqualTo("其他：大學講師");
    }

    @Test
    void noneOfTheToolsWinsOverTickedTools() throws Exception {
        Map<String, Object> answers = requiredOnly();
        answers.put("tools", List.of("ChatGPT", "都沒用"));
        assertThat(post("/survey/content", answers).body()).contains("qf-form-status ok");
        assertThat(lastColumn("tools")).isEqualTo("都沒用");
    }

    @Test
    void followUpIsKeptOnlyAfterTheTwoAnswersThatAskIt() throws Exception {
        Map<String, Object> answers = requiredOnly();
        answers.put("afterDelivery", "沒人問");
        answers.put("afterDeliveryCost", "不該留下");
        assertThat(post("/survey/content", answers).body()).contains("qf-form-status ok");
        assertThat(lastColumn("after_delivery_cost")).isNull();
    }

    @Test
    void blindLockedIsWhatTheBrowserSaysOneOrZero() throws Exception {
        Map<String, Object> answers = requiredOnly();
        answers.put("blindLocked", "1");
        assertThat(post("/survey/content", answers).body()).contains("qf-form-status ok");
        assertThat(lastColumn("blind_locked")).isEqualTo("1");
        answers.remove("blindLocked");
        assertThat(post("/en/survey/content", answers).body()).contains("qf-form-status ok");
        assertThat(lastColumn("blind_locked")).isEqualTo("0");
        answers.put("blindLocked", "yes");
        assertThat(post("/survey/content", answers).body()).contains("qf-form-status ok");
        assertThat(lastColumn("blind_locked")).isEqualTo("0");
    }

    @Test
    void withoutJavaScriptTheNoteAndNextStayHidden() throws Exception {
        String zh = get("/survey/content").body();
        assertThat(zh).containsPattern("<p class=\"sc-lock-note\" data-blind-note hidden>看到價格之後就不能改了，這樣我們才知道您原本心裡的價格。</p>")
            .containsPattern("<button type=\"button\" class=\"qf-btn sc-next\" data-blind-next hidden>下一步")
            .contains("<div data-reveal-block>");
        String en = get("/en/survey/content").body();
        assertThat(en).contains("data-blind-note hidden>Once you have seen the price", "data-blind-next hidden>Next");
    }

    // ---- helpers ----

    static Map<String, Object> requiredOnly() {
        Map<String, Object> f = new LinkedHashMap<>();
        f.put("role", "行銷／內容工作室");
        f.put("teamSize", "1 人");
        f.put("monthlyPieces", "4–10");
        f.put("blindUsd", "80");
        f.put("revealBuy", "會買");
        f.put("revealRenew", "會");
        f.put("name", "測試");
        f.put("contact", "test@example.com");
        return f;
    }

    static Map<String, Object> fullAnswers() {
        Map<String, Object> f = new LinkedHashMap<>();
        f.put("role", "研究型創作者（部落格、電子報、YouTube…）");
        f.put("teamSize", "2–5");
        f.put("monthlyPieces", "11–30");
        f.put("lastPiece", "一份市場報告，交給客戶");
        f.put("howChecked", "一個一個點開原始網址");
        f.put("checkHours", "1.5");
        f.put("afterDelivery", "有人指出錯誤");
        f.put("afterDeliveryCost", "要重做一份");
        f.put("tools", List.of("Claude", "Perplexity", "其他"));
        f.put("toolsOther", "Notion AI");
        f.put("toolCount", "3–4");
        f.put("moveHours", "2");
        f.put("formats", List.of("電子報", "短影音、影片腳本"));
        f.put("blindUsd", "120");
        f.put("revealBuy", "會考慮");
        f.put("revealRenew", "看當時的更新內容");
        f.put("contact", "+886 912 345 678");
        f.put("trial", "願意");
        return f;
    }

    List<String> lastRow() throws Exception {
        return row("SELECT lang, role, tools, formats, blind_price, reveal_buy, reveal_renew, after_delivery,"
            + " after_delivery_cost, check_hours, blind_locked, name FROM survey_content ORDER BY id DESC LIMIT 1");
    }

    String lastColumn(String __column) throws Exception {
        return row("SELECT " + __column + " FROM survey_content ORDER BY id DESC LIMIT 1").get(0);
    }

    static List<String> row(String __sql) throws Exception {
        try (Connection c = DriverManager.getConnection("jdbc:sqlite:" + SurveyControllerTest.DB);
             Statement st = c.createStatement(); ResultSet rs = st.executeQuery(__sql)) {
            rs.next();
            List<String> out = new ArrayList<>();
            for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) out.add(rs.getString(i));
            return out;
        }
    }

    HttpResponse<String> get(String __path) throws Exception {
        return client.send(HttpRequest.newBuilder(URI.create("http://localhost:" + port + __path)).GET().build(),
            HttpResponse.BodyHandlers.ofString());
    }

    /** A form post; a List value sends the name once per item, as ticked checkboxes do. */
    HttpResponse<String> post(String __path, Map<String, Object> __form) throws Exception {
        List<String> parts = new ArrayList<>();
        __form.forEach((k, v) -> {
            List<?> values = v instanceof List<?> l ? l : List.of(v);
            values.forEach(x -> parts.add(URLEncoder.encode(k, StandardCharsets.UTF_8) + "=" + URLEncoder.encode(x.toString(), StandardCharsets.UTF_8)));
        });
        return client.send(HttpRequest.newBuilder(URI.create("http://localhost:" + port + __path))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(parts.stream().collect(Collectors.joining("&")))).build(),
            HttpResponse.BodyHandlers.ofString());
    }
}
