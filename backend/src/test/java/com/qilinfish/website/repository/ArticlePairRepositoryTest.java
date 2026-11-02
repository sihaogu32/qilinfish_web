package com.qilinfish.website.repository;

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

/** content/article-pairs.txt: reading and start-up checks (spec behaviour 5 and the pair part of 6). */
class ArticlePairRepositoryTest {

    @TempDir
    Path content;

    ArticlePackageRepository articles;

    @BeforeEach
    void copyFixtures() throws IOException {
        Path from = ArticlePackageRepositoryTest.FIXTURES;
        try (Stream<Path> all = Files.walk(from)) {
            for (Path p : all.toList()) {
                Path to = content.resolve("articles").resolve(from.relativize(p).toString());
                if (Files.isDirectory(p)) Files.createDirectories(to);
                else Files.copy(p, to, StandardCopyOption.REPLACE_EXISTING);
            }
        }
        articles = new ArticlePackageRepository(content.resolve("articles").toString());
    }

    @Test
    void noFileMeansNoPairs() throws IOException {
        ArticlePairRepository pairs = new ArticlePairRepository(articles);
        assertThat(pairs.getFile()).isEqualTo(content.resolve("article-pairs.txt").toAbsolutePath().normalize());
        assertThat(pairs.findCounterpart("test-zh-video")).isEmpty();
    }

    @Test
    void aPairWorksBothWays() throws IOException {
        ArticlePairRepository pairs = pairs("# comment\n\n  test-zh-video   =  test-en-image  \n");
        assertThat(pairs.findCounterpart("test-zh-video")).contains("test-en-image");
        assertThat(pairs.findCounterpart("test-en-image")).contains("test-zh-video");
        assertThat(pairs.findCounterpart("test-zh-solo")).isEmpty();
    }

    @Test
    void bothArticlesMustExist() {
        fails("test-zh-video = no-such-article\n", "line 1: 'no-such-article' is not an article package");
    }

    @Test
    void oneZhAndOneEn() {
        fails("test-zh-video = test-zh-solo\n", "line 1: 'test-zh-video' and 'test-zh-solo' are both zh");
    }

    @Test
    void atMostOnePairPerArticle() {
        fails("test-zh-video = test-en-image\ntest-zh-solo = test-en-image\n",
            "line 2: 'test-en-image' is already paired on line 1");
    }

    @Test
    void lineMustBeAPair() {
        fails("test-zh-video test-en-image\n", "line 1: 'test-zh-video test-en-image' must be '<slug> = <slug>'");
        fails("test-zh-video = test-zh-video\n", "line 1: 'test-zh-video' is paired with itself");
    }

    private ArticlePairRepository pairs(String __text) throws IOException {
        Files.writeString(content.resolve("article-pairs.txt"), __text, StandardCharsets.UTF_8);
        return new ArticlePairRepository(articles);
    }

    private void fails(String __text, String __message) {
        assertThatThrownBy(() -> pairs(__text))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("article-pairs.txt")
            .hasMessageContaining(__message);
    }
}
