package com.qilinfish.website.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Spec D59: with no article with evidence marks in a language, that page has no part 3 at all (title included).
 * The Chinese fixtures are copied with every data-e removed, so the Chinese page has none.
 * F12: the demo must be found at start-up, and a package with marks is a real example in its own language; so this
 * class checks the Chinese page (both demos read the English package), ProductsContentNoCasesEnTest the English one.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
    "qilinfish.survey.db-path=target/test-survey.db",
    "qilinfish.mail.enabled=false",
    "qilinfish.demo.zh=test-en-image:E1",
    "qilinfish.demo.en=test-en-image:E1"
})
class ProductsContentNoCasesTest {

    static final Path FIXTURES = Path.of("src/test/resources/articles");

    /** The fixtures in a temporary folder, with every data-e removed from the packages whose name starts with __prefix. */
    static Path fixturesWithoutMarks(String __prefix) throws IOException {
        Path dir = Files.createTempDirectory("articles-unmarked");
        try (Stream<Path> all = Files.walk(FIXTURES)) {
            for (Path p : all.toList()) {
                Path rel = FIXTURES.relativize(p);
                Path to = dir.resolve(rel.toString());
                if (Files.isDirectory(p)) {
                    Files.createDirectories(to);
                } else if (p.getFileName().toString().equals("body.html") && rel.getName(0).toString().startsWith(__prefix)) {
                    Files.writeString(to, Files.readString(p, StandardCharsets.UTF_8).replaceAll(" data-e=\"[^\"]*\"", ""), StandardCharsets.UTF_8);
                } else {
                    Files.copy(p, to);
                }
            }
        }
        return dir;
    }

    @DynamicPropertySource
    static void unmarkedArticles(DynamicPropertyRegistry __registry) throws IOException {
        Path dir = fixturesWithoutMarks("test-zh-");
        __registry.add("qilinfish.articles.dir", dir::toString);
    }

    @Autowired
    TestRestTemplate http;

    @Test
    void partThreeIsLeftOutWhenNoArticleHasEvidenceMarks() {
        String zh = http.getForObject("/products/content", String.class);
        assertThat(zh).doesNotContain("實際案例", "pc.cases", "data-pc-cases", "CASES // made this way", "bl-card");
        // part 2 runs straight into part 4
        assertThat(zh).containsSubsequence("為什麼平常做不到", "怎麼付費");
    }
}
