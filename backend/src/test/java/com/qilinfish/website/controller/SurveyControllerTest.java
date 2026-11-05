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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The verification survey at /survey/verify, the old /survey address (GET 301, POST still accepted) and the
 * verification product page at /products/verify. Over a real server: /en is a servlet forward.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
    "qilinfish.articles.dir=src/test/resources/articles",
    "qilinfish.survey.db-path=target/test-survey.db",
    "qilinfish.mail.enabled=false"
})
class SurveyControllerTest {

    static final String DB = "target/test-survey.db";

    @LocalServerPort
    int port;

    final HttpClient client = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NEVER).build();

    @Test
    void oldSurveyAddressMovedForGoodWithItsWholeQuery() throws Exception {
        assertMoved("/survey", "/survey/verify");
        assertMoved("/survey?pick=verify", "/survey/verify?pick=verify");
        assertMoved("/en/survey", "/en/survey/verify");
        assertMoved("/en/survey?pick=verify", "/en/survey/verify?pick=verify");
        assertMoved("/survey?pick=verify&utm_source=x", "/survey/verify?pick=verify&utm_source=x");
    }

    @Test
    void postToTheOldAddressIsStillSaved() throws Exception {
        for (String path : new String[] { "/survey", "/en/survey", "/survey/verify" }) {
            long before = count("survey");
            HttpResponse<String> r = post(path, verifyAnswers());
            assertThat(r.statusCode()).as(path).isEqualTo(200);
            assertThat(r.body()).as(path).contains("qf-form-status ok");
            assertThat(count("survey")).as(path).isEqualTo(before + 1);
        }
    }

    @Test
    void surveyAndProductPagesAnswerAtTheirNewAddresses() throws Exception {
        assertThat(get("/survey/verify").statusCode()).isEqualTo(200);
        assertThat(get("/en/survey/verify").statusCode()).isEqualTo(200);
        String zh = get("/products/verify").body();
        assertThat(zh).contains("href=\"/survey/verify?pick=verify\"", "href=\"/survey/verify\"", "href=\"/contact\"");
        String en = get("/en/products/verify").body();
        assertThat(en).contains("href=\"/en/survey/verify?pick=verify\"", "href=\"/en/survey/verify\"", "href=\"/en/contact\"");
        assertThat(get("/survey/verify").body()).contains("action=\"/survey/verify\"");
        assertThat(get("/en/survey/verify").body()).contains("action=\"/en/survey/verify\"");
    }

    // ---- helpers ----

    static Map<String, String> verifyAnswers() {
        Map<String, String> f = new LinkedHashMap<>();
        f.put("role", "工程師");
        f.put("orgSize", "1–10 人");
        f.put("situation", "none");
        f.put("payOption", "none");
        f.put("name", "測試");
        f.put("company", "測試公司");
        f.put("contact", "test@example.com");
        return f;
    }

    void assertMoved(String __path, String __location) throws Exception {
        HttpResponse<String> r = get(__path);
        assertThat(r.statusCode()).as(__path).isEqualTo(301);
        assertThat(r.headers().firstValue("Location")).as(__path)
            .hasValueSatisfying(l -> assertThat(l.replaceFirst("^http://localhost:\\d+", "")).isEqualTo(__location));
    }

    HttpResponse<String> get(String __path) throws Exception {
        return client.send(HttpRequest.newBuilder(URI.create("http://localhost:" + port + __path)).GET().build(),
            HttpResponse.BodyHandlers.ofString());
    }

    HttpResponse<String> post(String __path, Map<String, String> __form) throws Exception {
        String body = __form.entrySet().stream()
            .map(e -> URLEncoder.encode(e.getKey(), StandardCharsets.UTF_8) + "=" + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
            .collect(Collectors.joining("&"));
        return client.send(HttpRequest.newBuilder(URI.create("http://localhost:" + port + __path))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body)).build(),
            HttpResponse.BodyHandlers.ofString());
    }

    /** Rows in a table of the test database; 0 when the table does not exist yet. */
    static long count(String __table) throws Exception {
        try (Connection c = DriverManager.getConnection("jdbc:sqlite:" + DB); Statement st = c.createStatement()) {
            try (ResultSet t = st.executeQuery("SELECT COUNT(*) FROM sqlite_master WHERE type='table' AND name='" + __table + "'")) {
                if (t.next() && t.getLong(1) == 0) return 0;
            }
            try (ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM " + __table)) {
                rs.next();
                return rs.getLong(1);
            }
        }
    }
}
