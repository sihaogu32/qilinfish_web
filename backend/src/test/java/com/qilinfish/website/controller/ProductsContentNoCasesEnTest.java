package com.qilinfish.website.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.io.IOException;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/** Spec D59 on the English page: the English fixture has no data-e, both demos read the Chinese package (F12). */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
    "qilinfish.survey.db-path=target/test-survey.db",
    "qilinfish.mail.enabled=false",
    "qilinfish.demo.zh=test-zh-solo:E1",
    "qilinfish.demo.en=test-zh-solo:E1"
})
class ProductsContentNoCasesEnTest {

    @DynamicPropertySource
    static void unmarkedArticles(DynamicPropertyRegistry __registry) throws IOException {
        Path dir = ProductsContentNoCasesTest.fixturesWithoutMarks("test-en-");
        __registry.add("qilinfish.articles.dir", dir::toString);
    }

    @Autowired
    TestRestTemplate http;

    @Test
    void partThreeIsLeftOutWhenNoArticleHasEvidenceMarks() {
        String en = http.getForObject("/en/products/content", String.class);
        assertThat(en).doesNotContain("Real examples", "pc.cases", "data-pc-cases", "bl-card");
        assertThat(en).containsSubsequence("Why it usually can", "How you pay");
    }
}
