package com.qilinfish.website.vb;

import com.qilinfish.website.model.BlogPostModel;

import java.util.List;
import java.util.Set;

/**
 * ViewBinder for the blog list and post pages — the "VM" in MVVM.
 * Wraps the post data with view-only fields (page language, which covers have a picture yet).
 * Fixed header copy comes from i18n/blog.properties, not from here.
 */
public class BlogViewBinder {

    private boolean en;                  // true on /en/... pages; picks the English title and summary
    private List<BlogPostModel> posts;
    private BlogPostModel post;          // set on a single post page only
    private Set<String> readyCovers;     // cover stems whose .jpg exists; the rest render as a poster block

    public BlogViewBinder() { }

    public boolean isEn() { return en; }
    public void setEn(boolean __en) { this.en = __en; }

    public List<BlogPostModel> getPosts() { return posts; }
    public void setPosts(List<BlogPostModel> __list) { this.posts = __list; }

    public BlogPostModel getPost() { return post; }
    public void setPost(BlogPostModel __post) { this.post = __post; }

    public Set<String> getReadyCovers() { return readyCovers; }
    public void setReadyCovers(Set<String> __readyCovers) { this.readyCovers = __readyCovers; }

    public boolean hasCover(BlogPostModel __post) {
        return __post != null && readyCovers != null && readyCovers.contains(__post.getCover());
    }

    /** Post title in the page language. */
    public String titleOf(BlogPostModel __post) { return __post.title(en); }

    /** Post summary in the page language. */
    public String summaryOf(BlogPostModel __post) { return __post.summary(en); }
}
