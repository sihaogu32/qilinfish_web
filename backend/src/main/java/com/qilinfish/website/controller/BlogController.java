package com.qilinfish.website.controller;

import com.qilinfish.website.model.BlogPostModel;
import com.qilinfish.website.vb.BlogViewBinder;
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
 * Controller for the blog. A new post = one entry in POSTS (newest first) + templates/blog/<slug>.html.
 */
@Controller
public class BlogController {

    private static final List<BlogPostModel> POSTS = List.of(
        new BlogPostModel("shadow-ai",
            "您的公司知道員工在用哪些 AI 嗎？",
            "將近一半用 AI 的員工承認用法違反公司規定。這篇整理國際調查、真實事件、各地規範，以及台灣的現況。",
            "2026-10-26",
            "blog_shadow_ai")
    );

    @GetMapping("/blog")
    public String renderBlog(Model __model) {
        BlogViewBinder vb = buildBlogBinder();
        vb.setPageTitle("文章");
        vb.setPageSubtitle("NOTES // what we have been reading");
        __model.addAttribute("vb", vb);
        __model.addAttribute("activeNav", "blog");
        return "blog";
    }

    @GetMapping("/blog/{slug}")
    public String renderPost(@PathVariable("slug") String __slug, Model __model) {
        BlogPostModel post = POSTS.stream()
            .filter(p -> p.getSlug().equals(__slug))
            .findFirst()
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        BlogViewBinder vb = buildBlogBinder();
        vb.setPost(post);
        __model.addAttribute("vb", vb);
        __model.addAttribute("activeNav", "blog");
        // only slugs from POSTS reach here, so the view name never comes from raw input
        return "blog/" + post.getSlug();
    }

    private BlogViewBinder buildBlogBinder() {
        BlogViewBinder vb = new BlogViewBinder();
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
