package com.qilinfish.website.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.qilinfish.website.model.ArticlePackageModel;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Range;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Reads every article package under qilinfish.articles.dir once, at start-up.
 * A package is a folder <slug>/ holding article.json, body.html and media/ (see README, "文章包").
 *
 * Fail fast: any broken package stops the app; the message names every package and what is wrong with it.
 * Only media/ is public: body.html writes media/<file>, served at /articles/<slug>/media/<file>.
 */
@Repository
public class ArticlePackageRepository {

    public static final String URL_PREFIX = "/articles/";
    public static final String MEDIA = "media/";

    private static final Pattern FRAME_TAG = Pattern.compile("<\\s*(html|head|style)[\\s>/]", Pattern.CASE_INSENSITIVE);
    private static final Pattern MEDIA_ATTR = Pattern.compile("(\\s(?:src|poster|href)\\s*=\\s*\")media/([^\"#?]*)");

    private final Path dir;
    private final ObjectMapper mapper = new ObjectMapper().enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    private final Map<String, ArticlePackageModel> bySlug = new LinkedHashMap<>();   // newest first

    public ArticlePackageRepository(@Value("${qilinfish.articles.dir}") String __dir) throws IOException {
        this.dir = Path.of(__dir).toAbsolutePath().normalize();
        loadAll();
    }

    public Path getDir() { return dir; }

    public Optional<ArticlePackageModel> findBySlug(String __slug) { return Optional.ofNullable(bySlug.get(__slug)); }

    /** Every package slug, newest first. */
    public List<String> findAllSlugs() { return List.copyOf(bySlug.keySet()); }

    /** Articles in one language, newest first. */
    public List<ArticlePackageModel> findByLang(boolean __en) {
        return bySlug.values().stream().filter(a -> a.isEn() == __en).toList();
    }

    /** Public URL of a file inside a package, e.g. media/hero.jpg -> /articles/<slug>/media/hero.jpg; null stays null. */
    public static String mediaUrl(ArticlePackageModel __article, String __file) {
        return __file == null ? null : URL_PREFIX + __article.getSlug() + "/" + __file;
    }

    private void loadAll() throws IOException {
        if (!Files.isDirectory(dir)) {
            throw new IllegalStateException("qilinfish.articles.dir (ARTICLES_DIR) is not a folder: " + dir);
        }
        List<String> problems = new ArrayList<>();
        List<ArticlePackageModel> found = new ArrayList<>();
        try (Stream<Path> children = Files.list(dir)) {
            for (Path pkg : children.filter(Files::isDirectory).sorted().toList()) {
                ArticlePackageModel a = loadOne(pkg, problems);
                if (a != null) found.add(a);
            }
        }
        if (!problems.isEmpty()) {
            throw new IllegalStateException("Broken article packages in " + dir + ":\n  - " + String.join("\n  - ", problems));
        }
        found.sort(Comparator.comparing(ArticlePackageModel::getDate).reversed().thenComparing(ArticlePackageModel::getSlug));
        found.forEach(a -> bySlug.put(a.getSlug(), a));
    }

    /** Returns the package, or null after adding what is wrong with it to __problems. */
    private ArticlePackageModel loadOne(Path __pkg, List<String> __problems) throws IOException {
        String name = __pkg.getFileName().toString();
        int before = __problems.size();
        Path json = __pkg.resolve("article.json");
        Path bodyFile = __pkg.resolve("body.html");
        if (!Files.isRegularFile(json)) { __problems.add(name + ": article.json not found"); }
        if (!Files.isRegularFile(bodyFile)) { __problems.add(name + ": body.html not found"); }
        if (__problems.size() > before) return null;

        ArticlePackageModel a;
        try {
            a = mapper.readValue(json.toFile(), ArticlePackageModel.class);
        } catch (UnrecognizedPropertyException e) {
            __problems.add(name + ": article.json has an unknown field '" + e.getPropertyName() + "'");
            return null;
        } catch (JsonProcessingException e) {
            __problems.add(name + ": article.json is not valid: " + e.getOriginalMessage());
            return null;
        }

        check(name.equals(a.getSlug()), __problems, name, "slug in article.json is '" + a.getSlug() + "', must match the folder name");
        check(ArticlePackageModel.ZH.equals(a.getLang()) || ArticlePackageModel.EN.equals(a.getLang()),
            __problems, name, "lang is '" + a.getLang() + "', must be zh or en");
        requireText(a.getTitle(), "title", name, __problems);
        requireText(a.getLede(), "lede", name, __problems);
        requireText(a.getSummary(), "summary", name, __problems);
        if (requireText(a.getDate(), "date", name, __problems)) {
            try {
                LocalDate.parse(a.getDate());
            } catch (DateTimeParseException e) {
                __problems.add(name + ": date '" + a.getDate() + "' must be yyyy-MM-dd");
            }
        }
        if (a.getHero() == null) {
            __problems.add(name + ": hero is missing (hero.image is required)");
        } else {
            if (requireText(a.getHero().getImage(), "hero.image", name, __problems)) {
                requireMedia(__pkg, a.getHero().getImage(), "hero.image", name, __problems);
            }
            if (a.getHero().getVideo() != null) {
                requireMedia(__pkg, a.getHero().getVideo(), "hero.video", name, __problems);
            }
        }
        if (requireText(a.getCover(), "cover", name, __problems)) {
            requireMedia(__pkg, a.getCover(), "cover", name, __problems);
        }

        String body = Files.readString(bodyFile, StandardCharsets.UTF_8);
        Matcher frame = FRAME_TAG.matcher(body);
        Set<String> frameTags = new LinkedHashSet<>();
        while (frame.find()) frameTags.add(frame.group(1).toLowerCase());
        for (String tag : frameTags) {
            __problems.add(name + ": body.html contains <" + tag + ">; it must hold the article content only");
        }
        Matcher media = MEDIA_ATTR.matcher(body);
        while (media.find()) {
            requireMedia(__pkg, MEDIA + media.group(2), "body.html", name, __problems);
        }
        int ctaAt = findCtaPosition(body, name, __problems);
        if (__problems.size() > before) return null;

        a.setBodyBeforeCta(pointMediaAtPublicPath(body.substring(0, ctaAt), name));
        a.setBodyAfterCta(pointMediaAtPublicPath(body.substring(ctaAt), name));
        return a;
    }

    /**
     * SP16: the article asks its question in its own box, .bl-invite, and the site adds the survey button at the end of it.
     * So there must be exactly one box, with no link of its own. Same rule as CompanyofOne's export.
     * Returns where the box's end tag starts in the original body.html (the markup itself is never re-serialised), or -1.
     */
    private static int findCtaPosition(String __body, String __name, List<String> __problems) {
        Document doc = Jsoup.parse(__body, "", Parser.htmlParser().setTrackPosition(true));
        Elements invites = doc.select(".bl-invite");
        if (invites.size() != 1) {
            __problems.add(__name + ": body.html must have exactly one .bl-invite box, found " + invites.size()
                + " (the site adds the survey button to it)");
            return -1;
        }
        Element invite = invites.first();
        if (!invite.select("a").isEmpty()) {
            __problems.add(__name + ": the .bl-invite box in body.html must not contain links (the site adds the survey button)");
            return -1;
        }
        Range end = invite.endSourceRange();
        if (!end.isTracked()) {
            __problems.add(__name + ": the .bl-invite box in body.html has no end tag");
            return -1;
        }
        return end.start().pos();
    }

    /** media/<file> in src, poster and href becomes /articles/<slug>/media/<file>, the only public part of a package. */
    private static String pointMediaAtPublicPath(String __html, String __name) {
        return MEDIA_ATTR.matcher(__html).replaceAll(m ->
            Matcher.quoteReplacement(m.group(1) + URL_PREFIX + __name + "/" + MEDIA + m.group(2)));
    }

    private static boolean requireText(String __value, String __field, String __name, List<String> __problems) {
        boolean ok = __value != null && !__value.isBlank();
        check(ok, __problems, __name, __field + " is missing");
        return ok;
    }

    /** A referenced file must be a real file inside the package's media/ folder, the only public part. */
    private static void requireMedia(Path __pkg, String __file, String __field, String __name, List<String> __problems) {
        Path mediaDir = __pkg.resolve("media").normalize();
        Path target = __pkg.resolve(__file).normalize();
        if (!__file.startsWith(MEDIA) || !target.startsWith(mediaDir)) {
            __problems.add(__name + ": " + __field + " '" + __file + "' must be a file under media/");
        } else if (!Files.isRegularFile(target)) {
            __problems.add(__name + ": " + __field + " '" + __file + "' not found");
        }
    }

    private static void check(boolean __ok, List<String> __problems, String __name, String __why) {
        if (!__ok) __problems.add(__name + ": " + __why);
    }
}
