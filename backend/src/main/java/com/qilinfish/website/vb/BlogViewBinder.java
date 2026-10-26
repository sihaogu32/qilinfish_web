package com.qilinfish.website.vb;

import com.qilinfish.website.model.BlogPostModel;

import java.util.List;
import java.util.Set;

/**
 * ViewBinder for the blog list and post pages — the "VM" in MVVM.
 * Wraps the post data with view-only fields (header copy, which covers have a picture yet).
 */
public class BlogViewBinder {

    private String pageTitle;
    private String pageSubtitle;
    private List<BlogPostModel> posts;
    private BlogPostModel post;          // set on a single post page only
    private Set<String> readyCovers;     // cover stems whose .jpg exists; the rest render as a poster block

    public BlogViewBinder() { }

    public String getPageTitle() { return pageTitle; }
    public void setPageTitle(String __pageTitle) { this.pageTitle = __pageTitle; }

    public String getPageSubtitle() { return pageSubtitle; }
    public void setPageSubtitle(String __pageSubtitle) { this.pageSubtitle = __pageSubtitle; }

    public List<BlogPostModel> getPosts() { return posts; }
    public void setPosts(List<BlogPostModel> __list) { this.posts = __list; }

    public BlogPostModel getPost() { return post; }
    public void setPost(BlogPostModel __post) { this.post = __post; }

    public Set<String> getReadyCovers() { return readyCovers; }
    public void setReadyCovers(Set<String> __readyCovers) { this.readyCovers = __readyCovers; }

    public boolean hasCover(BlogPostModel __post) {
        return __post != null && readyCovers != null && readyCovers.contains(__post.getCover());
    }
}
