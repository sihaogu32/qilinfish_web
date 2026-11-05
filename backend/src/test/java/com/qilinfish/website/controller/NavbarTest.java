package com.qilinfish.website.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

/** The navigation bar (fragments/layout.html :: navbar): no survey item (spec B28); survey pages mark "Products" (D44). */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
    "qilinfish.articles.dir=src/test/resources/articles",
    "qilinfish.survey.db-path=target/test-survey.db",
    "qilinfish.mail.enabled=false"
})
class NavbarTest {

    static final Pattern NAV = Pattern.compile("<nav class=\"qf-nav\">.*?</nav>", Pattern.DOTALL);

    @Autowired
    TestRestTemplate http;

    @Test
    void noSurveyItemInTheNavigationBar() {
        for (String path : new String[] { "/about", "/en/about", "/survey/verify", "/en/survey/verify" }) {
            // the language switch links to this same page, so it is left out
            String nav = nav(path).replaceAll("(?s)<span class=\"qf-nav__lang\">.*?</span>", "");
            assertThat(nav).as(path).doesNotContain("填寫問卷", "Take the survey", "nav.survey", "/survey");
        }
    }

    @Test
    void surveyPageMarksProductsAsTheCurrentPage() {
        assertThat(nav("/survey/verify")).contains("href=\"/products\" class=\"is-active\">產品方案</a>");
        assertThat(nav("/en/survey/verify")).contains("href=\"/en/products\" class=\"is-active\">Products</a>");
    }

    String nav(String __path) {
        ResponseEntity<String> r = http.getForEntity(__path, String.class);
        assertThat(r.getStatusCode()).as(__path).isEqualTo(HttpStatus.OK);
        Matcher m = NAV.matcher(r.getBody());
        assertThat(m.find()).as(__path + " has a navigation bar").isTrue();
        return m.group();
    }
}
