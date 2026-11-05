package com.qilinfish.website.controller;

import com.qilinfish.website.repository.ArticlePackageRepository;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** F12: the /products/content demo paragraphs are found once at start-up; a missing one stops the site. */
class ProductsContentDemoStartUpTest {

    final ArticlePackageRepository articles = new ArticlePackageRepository("src/test/resources/articles");

    ProductsContentDemoStartUpTest() throws IOException { }

    @Test
    void bothDemosFoundStartsUp() {
        assertThat(new ProductsController(null, articles, "test-zh-solo:E1", "test-en-image:E1")).isNotNull();
    }

    @Test
    void anENumberNoMarkPointsAtStopsStartUp() {
        assertThatThrownBy(() -> new ProductsController(null, articles, "test-zh-solo:E9", "test-en-image:E1"))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("/products/content demo not found")
            .hasMessageContaining("qilinfish.demo.zh (test-zh-solo:E9): test-zh-solo has no <p> with an evidence mark that points at 'E9'")
            .hasMessageNotContaining("qilinfish.demo.en");
    }

    @Test
    void everyMissingDemoIsNamed() {
        // test-zh-video has <sup class="cite"> but no data-e: no evidence mark at all
        assertThatThrownBy(() -> new ProductsController(null, articles, "test-zh-video:E1", "nope:E12"))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContainingAll("qilinfish.demo.zh (test-zh-video:E1): test-zh-video has no <p> with an evidence mark that points at 'E1'",
                "qilinfish.demo.en (nope:E12): article package 'nope' not found");
    }
}
