package com.qilinfish.website.controller;

import com.qilinfish.website.config.LangLocaleResolver;
import com.qilinfish.website.model.BlogPostModel;
import com.qilinfish.website.vb.BlogViewBinder;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Controller for the blog. A new post = one entry in POSTS (newest first)
 * + templates/blog/<slug>.html (Chinese) + templates/blog/en/<slug>.html (English).
 * Both bodies must exist or the app refuses to start (owner decision D8: both languages or not published).
 */
@Controller
public class BlogController {

    private static final List<BlogPostModel> POSTS = List.of(
        new BlogPostModel("shadow-ai",
            "您的公司知道員工在用哪些 AI 嗎？",
            "Does your company know which AI tools its staff are using?",
            "將近一半用 AI 的員工承認用法違反公司規定。這篇整理國際調查、真實事件、各地規範，以及台灣的現況。",
            "Nearly half of the employees who use AI admit that their use breaks company rules. This note gathers international surveys, real incidents, rules from several jurisdictions, and the situation in Taiwan.",
            "2026-10-26",
            "blog_shadow_ai")
    );

    /** Fail fast: every post needs its Chinese and its English body template. */
    @PostConstruct
    void checkPostTemplates() {
        for (BlogPostModel post : POSTS) {
            for (String view : List.of(zhView(post), enView(post))) {
                if (!new ClassPathResource("templates/" + view + ".html").exists()) {
                    throw new IllegalStateException("Blog post '" + post.getSlug() + "' is missing templates/" + view
                        + ".html (a post ships in both languages or not at all)");
                }
            }
        }
    }

    @GetMapping("/blog")
    public String renderBlog(HttpServletRequest __request, Model __model) {
        __model.addAttribute("vb", buildBlogBinder(__request));
        __model.addAttribute("activeNav", "blog");
        return "blog";
    }

    @GetMapping("/blog/{slug}")
    public String renderPost(@PathVariable("slug") String __slug, HttpServletRequest __request, Model __model) {
        BlogPostModel post = POSTS.stream()
            .filter(p -> p.getSlug().equals(__slug))
            .findFirst()
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        BlogViewBinder vb = buildBlogBinder(__request);
        vb.setPost(post);
        __model.addAttribute("vb", vb);
        __model.addAttribute("activeNav", "blog");
        // only slugs from POSTS reach here, so the view name never comes from raw input
        return vb.isEn() ? enView(post) : zhView(post);
    }

    private static String zhView(BlogPostModel __post) { return "blog/" + __post.getSlug(); }

    private static String enView(BlogPostModel __post) { return "blog/en/" + __post.getSlug(); }

    private BlogViewBinder buildBlogBinder(HttpServletRequest __request) {
        BlogViewBinder vb = new BlogViewBinder();
        vb.setEn(LangLocaleResolver.isEnglish(__request));
        vb.setPosts(POSTS);
        vb.setReadyCovers(findReadyCovers());
        return vb;
    }

    /** Covers are supplied later; until the .jpg is there the page shows a poster block instead of a broken image. */
    private Set<String> findReadyCovers() {
        return POSTS.stream()
            .map(BlogPostModel::getCover)
            .filter(c -> new ClassPathResource("static/media/" + c + ".jpg").exists())
            .collect(Collectors.toSet());
    }
}
