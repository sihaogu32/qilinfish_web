package com.qilinfish.website.controller;

import com.qilinfish.website.vb.AboutViewBinder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller for the index (home) page.
 *
 * Hero section uses a typewriter effect over a code-grid backdrop;
 * a short capability strip echoes content from AboutViewBinder.
 */
@Controller
public class HomeController {

    @GetMapping({"/", "/index"})
    public String renderHome(Model __model) {
        AboutViewBinder vb = new AboutViewBinder();
        vb.setStudioName("麒麟魚 Qilinfish");
        vb.setTagline("美感是『普適模式』被『主觀經驗』解構後的殘餘。");
        vb.setCapabilities(new String[] {
            "AI Agent 開發",
            "Web 應用",
            "專案設計",
            "自動化整合"
        });
        __model.addAttribute("vb", vb);
        __model.addAttribute("activeNav", "home");
        return "index";
    }
}
