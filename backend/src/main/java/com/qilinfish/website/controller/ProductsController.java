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
    public String renderProducts(@RequestParam(value = "tab", required = false, defaultValue = "legal-agent") String __tab,
                                 Model __model) {
        ProductsViewBinder vb = new ProductsViewBinder();
        vb.setPageTitle("產品方案");
        vb.setPageSubtitle("PRODUCTS // what we ship");
        vb.setActiveCategory(__tab);

        vb.setLegalAgentProducts(List.of(
            new ProductModel(
                "p-legal-01",
                "legal-agent-pro",
                "法律 Agent",
                "Legal Domain Intelligent Agent",
                "結合台灣現行法規與案例資料庫，協助使用者進行條文檢索、爭點整理、風險評估的智能助手。",
                new String[]{"全國法規即時檢索", "案例引用標註", "爭點摘要", "可整合企業內部法務系統"},
                "demo-ready"
            )
        ));

        vb.setCustomAgentProducts(List.of(
            new ProductModel(
                "p-custom-01",
                "agent-customization",
                "Agent 客製化",
                "Custom Agent Engineering",
                "依需求設計專屬 Agent — 從 prompt 工程、工具呼叫、到資料來源整合，全程量身打造。",
                new String[]{"領域知識微調", "工具/API 串接", "工作流自動化", "可離線部署選項"},
                "available"
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
