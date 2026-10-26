package com.qilinfish.website.controller;

import com.qilinfish.website.model.ScenarioModel;
import com.qilinfish.website.vb.ProductsViewBinder;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Locale;

/**
 * The copy lives in i18n/products.properties (Chinese, default) and i18n/products_en.properties (English).
 */
@Controller
public class ProductsController {

    private final MessageSource messages;

    public ProductsController(MessageSource __messages) {
        this.messages = __messages;
    }

    @GetMapping("/products")
    public String renderProducts(Model __model, Locale __locale) {
        ProductsViewBinder vb = new ProductsViewBinder();
        vb.setHeroTitle(msg("products.hero.title", __locale));
        vb.setHeroSubtitle(msg("products.hero.subtitle", __locale));

        vb.setScenarios(List.of(
            new ScenarioModel(msg("products.scene.brief", __locale), "verify", "scene_brief"),
            new ScenarioModel(msg("products.scene.agent", __locale), "verify", "scene_agent"),
            new ScenarioModel(msg("products.scene.explain", __locale), "explain", "scene_explain"),
            new ScenarioModel(msg("products.scene.legacy", __locale), "inventory", "scene_legacy")
        ));

        __model.addAttribute("vb", vb);
        __model.addAttribute("activeNav", "products");
        return "products";
    }

    private String msg(String __key, Locale __locale) {
        return messages.getMessage(__key, null, __locale);
    }
}
