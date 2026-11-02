package com.qilinfish.website.controller;

import com.qilinfish.website.config.LangLocaleResolver;
import com.qilinfish.website.config.LangPathFilter;
import com.qilinfish.website.model.ArticlePackageModel;
import com.qilinfish.website.repository.ArticlePackageRepository;
import com.qilinfish.website.repository.ArticlePairRepository;
import com.qilinfish.website.vb.BlogViewBinder;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Controller for the blog: /blog, /blog/{slug} (and the same under /en).
 * Every article is an article package from CompanyofOne (content/articles/<slug>/), shown through blog/article.html.
 * An article lives in its own language only: the other language's URL is a 404.
 */
@Controller
public class BlogController {

    /**
     * English URLs that moved for good (301), old slug -> new slug. One slug is one article (SP6):
     * shadow-ai used to answer in both languages; the English article is now shadow-ai-en.
     */
    private static final Map<String, String> EN_MOVED = Map.of("shadow-ai", "shadow-ai-en");

    private final ArticlePackageRepository articles;
    private final ArticlePairRepository pairs;

    public BlogController(ArticlePackageRepository __articles, ArticlePairRepository __pairs) {
        this.articles = __articles;
        this.pairs = __pairs;
    }

    @GetMapping("/blog")
    public String renderBlog(HttpServletRequest __request, Model __model) {
        BlogViewBinder vb = buildBlogBinder(__request);
        vb.setCards(buildCards(vb));
        __model.addAttribute("vb", vb);
        __model.addAttribute("activeNav", "blog");
        return "blog";
    }

    @GetMapping("/blog/{slug}")
    public String renderPost(@PathVariable("slug") String __slug, HttpServletRequest __request, Model __model) {
        BlogViewBinder vb = buildBlogBinder(__request);
        if (vb.isEn() && EN_MOVED.containsKey(__slug)) {
            __request.setAttribute(View.RESPONSE_STATUS_ATTRIBUTE, HttpStatus.MOVED_PERMANENTLY);
            return "redirect:" + LangPathFilter.EN_PREFIX + "/blog/" + EN_MOVED.get(__slug);
        }
        ArticlePackageModel article = articles.findBySlug(__slug)
            .filter(a -> a.isEn() == vb.isEn())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        __model.addAttribute("vb", vb);
        __model.addAttribute("activeNav", "blog");
        return renderArticle(article, vb, __model);
    }

    /**
     * One article package inside the site frame.
     * Language switch: to the counterpart when the article is paired (article-pairs.txt), and then both hreflang links;
     * otherwise to the other language's list, with no hreflang (an article is not translated).
     * og:image, canonical and hreflang are absolute, built from this request's scheme and host.
     */
    private String renderArticle(ArticlePackageModel __article, BlogViewBinder __vb, Model __model) {
        String self = "/blog/" + __article.getSlug();
        String site = ServletUriComponentsBuilder.fromCurrentContextPath().replaceQuery(null).build().toUriString();
        __vb.setArticle(__article);
        __vb.setHeroImageUrl(ArticlePackageRepository.mediaUrl(__article, __article.getHero().getImage()));
        __vb.setHeroVideoUrl(ArticlePackageRepository.mediaUrl(__article, __article.getHero().getVideo()));
        __vb.setPageUrl(site + (__article.isEn() ? LangPathFilter.EN_PREFIX : "") + self);
        __vb.setImageUrl(site + __vb.getHeroImageUrl());
        Optional<String> counterpart = pairs.findCounterpart(__article.getSlug());
        String other = counterpart.map(c -> "/blog/" + c).orElse("/blog");
        String zhUrl = __article.isEn() ? other : self;
        String enUrl = LangPathFilter.EN_PREFIX + (__article.isEn() ? self : other);
        __model.addAttribute("zhUrl", zhUrl);
        __model.addAttribute("enUrl", enUrl);
        if (counterpart.isPresent()) {
            __vb.setZhAlternateUrl(site + zhUrl);
            __vb.setEnAlternateUrl(site + enUrl);
        }
        return "blog/article";
    }

    private BlogViewBinder buildBlogBinder(HttpServletRequest __request) {
        BlogViewBinder vb = new BlogViewBinder();
        vb.setEn(LangLocaleResolver.isEnglish(__request));
        return vb;
    }

    /** List cards: the article packages in the page language, newest first. */
    private List<BlogViewBinder.Card> buildCards(BlogViewBinder __vb) {
        return articles.findByLang(__vb.isEn()).stream()
            .map(a -> new BlogViewBinder.Card(a.getSlug(), a.getTitle(), a.getSummary(), a.getDate(),
                ArticlePackageRepository.mediaUrl(a, a.getCover())))
            .toList();
    }
}
