package com.qilinfish.website.vb;

import java.util.List;

/**
 * ViewBinder for the products entry page (/products) — the "VM" in MVVM.
 * Two still cards, one per product (spec B1–B6); no video. Fixed copy comes from i18n/products_index*.properties (pe. keys).
 */
public class ProductsIndexViewBinder {

    /** One product card: the whole card links to the product page. */
    public static class Card {
        private final String title;
        private final String text;
        private final String forWhom;
        private final String image;        // under /media/
        private final String imageAlt;
        private final boolean aiImage;     // true: the picture is AI-generated and says so; the placeholder does not (D56)
        private final String path;         // site path without the language prefix, e.g. /products/verify

        public Card(String __title, String __text, String __forWhom, String __image, String __imageAlt, boolean __aiImage, String __path) {
            this.title = __title;
            this.text = __text;
            this.forWhom = __forWhom;
            this.image = __image;
            this.imageAlt = __imageAlt;
            this.aiImage = __aiImage;
            this.path = __path;
        }

        public String getTitle() { return title; }
        public String getText() { return text; }
        public String getForWhom() { return forWhom; }
        public String getImage() { return image; }
        public String getImageAlt() { return imageAlt; }
        public boolean isAiImage() { return aiImage; }
        public String getPath() { return path; }
    }

    private List<Card> cards;

    public ProductsIndexViewBinder() { }

    public List<Card> getCards() { return cards; }
    public void setCards(List<Card> __cards) { this.cards = __cards; }
}
