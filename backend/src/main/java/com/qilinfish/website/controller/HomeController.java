package com.qilinfish.website.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * The site has no separate home page; "/" lands on the about page.
 */
@Controller
public class HomeController {

    @GetMapping({"/", "/index"})
    public String renderHome() {
        return "redirect:/about";
    }
}
