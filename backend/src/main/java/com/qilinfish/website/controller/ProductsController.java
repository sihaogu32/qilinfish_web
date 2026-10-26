package com.qilinfish.website.controller;

import com.qilinfish.website.model.ScenarioModel;
import com.qilinfish.website.vb.ProductsViewBinder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class ProductsController {

    @GetMapping("/products")
    public String renderProducts(Model __model) {
        ProductsViewBinder vb = new ProductsViewBinder();
        vb.setHeroTitle("AI 說「做完了」，真的完成了嗎？");
        vb.setHeroSubtitle("整份看起來都對。往下查，錯誤才一個一個出現。");

        vb.setScenarios(List.of(
            new ScenarioModel("交出 AI 協助寫的書狀或報告之前", "verify", "scene_brief"),
            new ScenarioModel("AI 代理回報「做完了」", "verify", "scene_agent"),
            new ScenarioModel("主管、客戶、稽核問「這是怎麼來的」", "explain", "scene_explain"),
            new ScenarioModel("接手交接不齊的舊系統、準備系統遷移", "inventory", "scene_legacy")
        ));

        __model.addAttribute("vb", vb);
        __model.addAttribute("activeNav", "products");
        return "products";
    }
}
