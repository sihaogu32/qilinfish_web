package com.qilinfish.website.controller;

import com.qilinfish.website.model.ProductModel;
import com.qilinfish.website.vb.ProductsViewBinder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class ProductsController {

    @GetMapping("/products")
    public String renderProducts(@RequestParam(value = "tab", required = false, defaultValue = "ai-audit") String __tab,
                                 Model __model) {
        ProductsViewBinder vb = new ProductsViewBinder();
        vb.setPageTitle("產品方案");
        vb.setPageSubtitle("PRODUCTS // what we ship");
        vb.setActiveCategory(__tab);

        vb.setAiAuditProducts(List.of(
            new ProductModel(
                "p-audit-01",
                "ai-audit",
                "稽核與驗證 AI 系統",
                "AI Output Verification & Audit",
                "AI 說「做完了」，真的完成了嗎？\n"
                    + "AI 的回答常常看起來對，其實有錯。\n"
                    + "主管或客戶問「這是怎麼來的？」，您拿得出根據嗎？",
                new String[]{"逐句查核", "附上出處", "標出錯誤", "留下查核紀錄"},
                "coming-soon"
            )
        ));

        vb.setOtherProducts(List.of(
            new ProductModel(
                "p-other-01",
                "web-development",
                "Web 網頁開發",
                "Responsive Web Application",
                "從靜態形象站到後端動態應用，使用 Spring Boot、Thymeleaf、現代前端框架交付。",
                new String[]{"響應式設計", "SEO 友善", "雲端部署 (Cloudflare / AWS)", "後台管理系統"},
                "available"
            ),
            new ProductModel(
                "p-other-02",
                "project-design",
                "專案設計",
                "Project Architecture & Design",
                "需求訪談、系統規劃、技術選型、人力評估 — 把模糊的想法收斂成可執行的工程文件。",
                new String[]{"需求訪談", "系統架構圖", "技術選型建議", "工時估算"},
                "available"
            )
        ));

        __model.addAttribute("vb", vb);
        __model.addAttribute("activeNav", "products");
        return "products";
    }
}
