package com.qilinfish.website.repository;

import com.qilinfish.website.model.ArticlePackageModel;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Counterparts: a Chinese and an English article on the same topic (not translations of each other).
 * The site decides the pairs; they live in article-pairs.txt next to the articles folder (content/article-pairs.txt),
 * one pair per line, e.g. "solo-trust = solo-trust-en". Blank lines and lines starting with # are ignored.
 * No file means no pairs.
 *
 * Fail fast: a pair naming a missing article, two articles in the same language, or an article in more than one pair
 * stops the app; the message gives the line.
 */
@Repository
public class ArticlePairRepository {

    public static final String FILE_NAME = "article-pairs.txt";

    private final Path file;
    private final Map<String, String> counterparts = new HashMap<>();

    public ArticlePairRepository(ArticlePackageRepository __articles) throws IOException {
        this.file = __articles.getDir().resolveSibling(FILE_NAME);
        load(__articles);
    }

    public Path getFile() { return file; }

    /** Slug of the same-topic article in the other language, if the two are paired. */
    public Optional<String> findCounterpart(String __slug) { return Optional.ofNullable(counterparts.get(__slug)); }

    private void load(ArticlePackageRepository __articles) throws IOException {
        if (!Files.isRegularFile(file)) return;
        List<String> problems = new ArrayList<>();
        Map<String, Integer> seenOnLine = new HashMap<>();
        List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        for (int i = 0; i < lines.size(); i++) {
            int lineNo = i + 1;
            String line = lines.get(i).strip();
            if (line.isEmpty() || line.startsWith("#")) continue;
            String[] parts = line.split("=", -1);
            if (parts.length != 2 || parts[0].isBlank() || parts[1].isBlank()) {
                problems.add("line " + lineNo + ": '" + line + "' must be '<slug> = <slug>'");
                continue;
            }
            String a = parts[0].strip();
            String b = parts[1].strip();
            if (a.equals(b)) {
                problems.add("line " + lineNo + ": '" + a + "' is paired with itself");
                continue;
            }
            int before = problems.size();
            for (String slug : List.of(a, b)) {
                Integer earlier = seenOnLine.putIfAbsent(slug, lineNo);
                if (earlier != null) {
                    problems.add("line " + lineNo + ": '" + slug + "' is already paired on line " + earlier + " (an article has at most one counterpart)");
                }
            }
            Optional<ArticlePackageModel> left = __articles.findBySlug(a);
            Optional<ArticlePackageModel> right = __articles.findBySlug(b);
            if (left.isEmpty()) problems.add("line " + lineNo + ": '" + a + "' is not an article package");
            if (right.isEmpty()) problems.add("line " + lineNo + ": '" + b + "' is not an article package");
            if (left.isPresent() && right.isPresent() && left.get().getLang().equals(right.get().getLang())) {
                problems.add("line " + lineNo + ": '" + a + "' and '" + b + "' are both " + left.get().getLang()
                    + " (a pair is one zh and one en article)");
            }
            if (problems.size() == before) {
                counterparts.put(a, b);
                counterparts.put(b, a);
            }
        }
        if (!problems.isEmpty()) {
            throw new IllegalStateException("Broken " + file + ":\n  - " + String.join("\n  - ", problems));
        }
    }
}
