package com.qilinfish.website.controller;

import com.qilinfish.website.config.LangLocaleResolver;
import com.qilinfish.website.model.ScenarioModel;
import com.qilinfish.website.repository.ArticlePackageRepository;
import com.qilinfish.website.vb.BlogViewBinder;
import com.qilinfish.website.vb.ProductsContentViewBinder;
import com.qilinfish.website.vb.ProductsIndexViewBinder;
import com.qilinfish.website.vb.ProductsViewBinder;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * /products is the entry page to the two products (i18n/products_index*.properties, pe. keys);
 * /products/verify the verification product page, moved there unchanged (i18n/products*.properties);
 * /products/content the article-app product page (i18n/products_content*.properties, pc. keys).
 */
@Controller
public class ProductsController {

    private final MessageSource messages;
    private final ArticlePackageRepository articles;

    public ProductsController(MessageSource __messages, ArticlePackageRepository __articles) {
        this.messages = __messages;
        this.articles = __articles;
    }

    /** The entry page (spec B1–B6): two still cards, both AI-made pictures that say so (D63). */
    @GetMapping("/products")
    public String renderProductsIndex(Model __model, Locale __locale) {
        ProductsIndexViewBinder vb = new ProductsIndexViewBinder();
        vb.setCards(List.of(
            new ProductsIndexViewBinder.Card(msg("pe.verify.title", __locale), msg("pe.verify.text", __locale),
                msg("pe.verify.for", __locale), "report_clean.jpg", msg("pe.verify.alt", __locale), true, "/products/verify"),
            new ProductsIndexViewBinder.Card(msg("pe.content.title", __locale), msg("pe.content.text", __locale),
                msg("pe.content.for", __locale), "content_clean.jpg", msg("pe.content.alt", __locale), true, "/products/content")
        ));
        __model.addAttribute("vb", vb);
        return "products_index";
    }

    @GetMapping("/products/verify")
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

    /** The article-app product page (spec B8–B15). Copy: i18n/products_content*.properties, every key pc. */
    @GetMapping("/products/content")
    public String renderContentProduct(Model __model, Locale __locale, HttpServletRequest __request) {
        ProductsContentViewBinder vb = new ProductsContentViewBinder();
        // real examples: same rule as the line at the end of an article (B12 = B30), page language only (D46)
        vb.setCases(articles.findEvidenceMarkedByLang(LangLocaleResolver.isEnglish(__request)).stream()
            .map(a -> new BlogViewBinder.Card(a.getSlug(), a.getTitle(), a.getSummary(), a.getDate(),
                ArticlePackageRepository.mediaUrl(a, a.getCover())))
            .toList());
        vb.setDemo(demoSentences(__locale));
        vb.setEvidence(vb.getDemo().stream().map(ProductsContentViewBinder.Sentence::getEvidence).distinct()
            .map(id -> evidence(id, __locale)).toList());
        vb.setBrokenSteps(numbered("pc.why.broken.step.", __locale));
        vb.setLineSteps(numbered("pc.why.line.step.", __locale));
        __model.addAttribute("vb", vb);
        return "products_content";
    }

    /**
     * The demo excerpt, one sentence per pc.demo.<n>.text (n from 1 until the key is missing), so each language
     * keeps its own excerpt: Chinese from solo-trust, English from ai-handover (spec appendix B, C).
     */
    private List<ProductsContentViewBinder.Sentence> demoSentences(Locale __locale) {
        List<ProductsContentViewBinder.Sentence> out = new ArrayList<>();
        for (int n = 1; optional("pc.demo." + n + ".text", __locale) != null; n++) {
            String p = "pc.demo." + n;
            out.add(new ProductsContentViewBinder.Sentence(msg(p + ".text", __locale),
                optional(p + ".cite", __locale), optional(p + ".ev", __locale)));
        }
        return out;
    }

    /** One evidence panel from pc.ev.<__id>.*; quotes are pc.ev.<__id>.q.<n> (+ .note, .tr), n from 1. */
    private ProductsContentViewBinder.Evidence evidence(String __id, Locale __locale) {
        String p = "pc.ev." + __id;
        List<ProductsContentViewBinder.Quote> quotes = new ArrayList<>();
        for (int n = 1; optional(p + ".q." + n, __locale) != null; n++) {
            String q = p + ".q." + n;
            quotes.add(new ProductsContentViewBinder.Quote(optional(q + ".note", __locale), msg(q, __locale), optional(q + ".tr", __locale)));
        }
        return new ProductsContentViewBinder.Evidence(__id, msg(p + ".publisher", __locale), msg(p + ".title", __locale),
            msg(p + ".date", __locale), quotes, msg(p + ".qual", __locale), msg(p + ".url", __locale));
    }

    /** __prefix + 1, 2, ... until the key is missing. */
    private List<String> numbered(String __prefix, Locale __locale) {
        List<String> out = new ArrayList<>();
        for (int n = 1; optional(__prefix + n, __locale) != null; n++) out.add(msg(__prefix + n, __locale));
        return out;
    }

    /**
     * The message, or null when the key does not exist or is empty (unlike msg, which falls back to the key itself).
     * An English key that is missing falls back to the Chinese file, so the English file writes "key=" for "none".
     */
    private String optional(String __key, Locale __locale) {
        // a default of "" (not null): with setUseCodeAsDefaultMessage(true) a null default still returns the key
        String m = messages.getMessage(__key, null, "", __locale);
        return m == null || m.isBlank() ? null : m;
    }

    private String msg(String __key, Locale __locale) {
        return messages.getMessage(__key, null, __locale);
    }
}
