package com.qilinfish.website.repository;

import com.qilinfish.website.model.SurveyContentFormModel;
import com.qilinfish.website.model.SurveyFormModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Stores survey submissions in a single SQLite file, one row per submission.
 *
 * Reading the data: copy the file off the server and open it in
 * DB Browser for SQLite (see README, "問卷資料").
 * Columns hold human-readable values so the table can be filtered and
 * exported to CSV as-is.
 */
@Repository
public class SurveyRepository {

    private static final Logger log = LoggerFactory.getLogger(SurveyRepository.class);
    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final ZoneId TAIPEI = ZoneId.of("Asia/Taipei");

    private static final String CREATE_TABLE =
        "CREATE TABLE IF NOT EXISTS survey ("
      + " id INTEGER PRIMARY KEY AUTOINCREMENT,"
      + " created_at TEXT NOT NULL,"
      + " name TEXT, company TEXT, contact TEXT,"
      + " role TEXT, role_other TEXT, org_size TEXT,"
      + " situation TEXT, occurred_when TEXT,"
      + " what_happened TEXT, steps TEXT, outcome TEXT,"
      + " hours TEXT, people TEXT, extra_spend TEXT, worst_case TEXT,"
      + " tried_other TEXT, hardest_step TEXT,"
      + " why_not_needed TEXT,"
      + " message TEXT,"
      + " pay_min TEXT, pay_max TEXT, pay_option TEXT,"
      + " lang TEXT)";

    private static final String INSERT =
        "INSERT INTO survey (created_at, name, company, contact, role, role_other, org_size,"
      + " situation, occurred_when, what_happened, steps, outcome, hours, people, extra_spend,"
      + " worst_case, tried_other, hardest_step, why_not_needed, message,"
      + " pay_min, pay_max, pay_option, lang)"
      + " VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

    /**
     * The article-app survey (/survey/content), same file, its own table (spec D34). One column per question;
     * a multiple-choice answer is its Chinese options joined with "、"; blind_locked is 1 when the browser showed
     * the reveal questions only after "Next" (a hint, not a guarantee: D33).
     */
    private static final String CREATE_CONTENT_TABLE =
        "CREATE TABLE IF NOT EXISTS survey_content ("
      + " id INTEGER PRIMARY KEY AUTOINCREMENT,"
      + " created_at TEXT NOT NULL,"
      + " lang TEXT,"
      + " role TEXT, team_size TEXT, monthly_pieces TEXT,"
      + " last_piece TEXT, how_checked TEXT, check_hours TEXT, after_delivery TEXT, after_delivery_cost TEXT,"
      + " tools TEXT, tool_count TEXT, move_hours TEXT, formats TEXT,"
      + " blind_price TEXT, reveal_buy TEXT, reveal_renew TEXT,"
      + " name TEXT, contact TEXT, trial TEXT,"
      + " blind_locked INTEGER NOT NULL DEFAULT 0)";

    private static final String INSERT_CONTENT =
        "INSERT INTO survey_content (created_at, lang, role, team_size, monthly_pieces,"
      + " last_piece, how_checked, check_hours, after_delivery, after_delivery_cost,"
      + " tools, tool_count, move_hours, formats, blind_price, reveal_buy, reveal_renew,"
      + " name, contact, trial, blind_locked)"
      + " VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

    private final String dbPath;

    public SurveyRepository(@Value("${qilinfish.survey.db-path}") String __dbPath) {
        this.dbPath = __dbPath;
    }

    /**
     * Save one submission. Returns true on success, false on any failure (logged).
     * Synchronized: SQLite allows one writer at a time, and traffic here is tiny.
     * __lang is the page language the respondent answered in ("zh" or "en"); choice values are
     * stored as the same Chinese strings in both languages, so rows can be filtered together.
     */
    public synchronized boolean saveSurvey(SurveyFormModel __form, String __lang) {
        try {
            Path parent = Path.of(dbPath).toAbsolutePath().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath)) {
                try (Statement st = conn.createStatement()) {
                    st.execute(CREATE_TABLE);
                }
                boolean none = "none".equals(__form.getSituation());
                try (PreparedStatement ps = conn.prepareStatement(INSERT)) {
                    int i = 1;
                    ps.setString(i++, ZonedDateTime.now(TAIPEI).format(TS));
                    ps.setString(i++, trim(__form.getName()));
                    ps.setString(i++, trim(__form.getCompany()));
                    ps.setString(i++, trim(__form.getContact()));
                    ps.setString(i++, trim(__form.getRole()));
                    ps.setString(i++, "其他".equals(__form.getRole()) ? trim(__form.getRoleOther()) : null);
                    ps.setString(i++, trim(__form.getOrgSize()));
                    ps.setString(i++, situationLabel(__form.getSituation(), __form.getSituationOther()));
                    // story answers only count when something happened; the none-branch answer only when nothing did
                    ps.setString(i++, none ? null : trim(__form.getOccurredWhen()));
                    ps.setString(i++, none ? null : trim(__form.getWhatHappened()));
                    ps.setString(i++, none ? null : trim(__form.getSteps()));
                    ps.setString(i++, none ? null : trim(__form.getOutcome()));
                    ps.setString(i++, none ? null : trim(__form.getHours()));
                    ps.setString(i++, none ? null : trim(__form.getPeople()));
                    ps.setString(i++, none ? null : extraSpendLabel(__form.getExtraSpend(), __form.getExtraSpendUsd()));
                    ps.setString(i++, none ? null : trim(__form.getWorstCase()));
                    ps.setString(i++, none ? null : trim(__form.getTriedOther()));
                    ps.setString(i++, none ? null : trim(__form.getHardestStep()));
                    ps.setString(i++, none ? trim(__form.getWhyNotNeeded()) : null);
                    ps.setString(i++, trim(__form.getMessage()));
                    // a picked option wins over typed amounts, so the range is stored only without one
                    String payOption = optionLabel(__form.getPayOption(), "不會付費");
                    ps.setString(i++, payOption == null ? SurveyFormModel.normalizeUsd(__form.getPayMin()) : null);
                    ps.setString(i++, payOption == null ? SurveyFormModel.normalizeUsd(__form.getPayMax()) : null);
                    ps.setString(i++, payOption);
                    ps.setString(i++, __lang);
                    ps.executeUpdate();
                }
            }
            return true;
        } catch (Exception ex) {
            log.error("Failed to save survey to {}", dbPath, ex);
            return false;
        }
    }

    /**
     * Save one article-app survey submission (/survey/content) into survey_content. Returns true on success,
     * false on any failure (logged). __form has passed SurveyContentController's checks; __lang is "zh" or "en".
     */
    public synchronized boolean saveContentSurvey(SurveyContentFormModel __form, String __lang) {
        try {
            Path parent = Path.of(dbPath).toAbsolutePath().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath)) {
                try (Statement st = conn.createStatement()) {
                    st.execute(CREATE_CONTENT_TABLE);
                }
                // List.of(...).contains(null) throws, so a blank answer is checked first
                String after = __form.getAfterDelivery();
                boolean followUp = after != null && SurveyContentFormModel.AFTER_DELIVERY_FOLLOW_UP.contains(after);
                try (PreparedStatement ps = conn.prepareStatement(INSERT_CONTENT)) {
                    int i = 1;
                    ps.setString(i++, ZonedDateTime.now(TAIPEI).format(TS));
                    ps.setString(i++, __lang);
                    ps.setString(i++, withOther(__form.getRole(), __form.getRoleOther()));
                    ps.setString(i++, trim(__form.getTeamSize()));
                    ps.setString(i++, trim(__form.getMonthlyPieces()));
                    ps.setString(i++, trim(__form.getLastPiece()));
                    ps.setString(i++, trim(__form.getHowChecked()));
                    ps.setString(i++, SurveyFormModel.normalizeUsd(__form.getCheckHours()));
                    ps.setString(i++, trim(__form.getAfterDelivery()));
                    // 7a is only asked after "someone asked for sources / pointed out an error"
                    ps.setString(i++, followUp ? trim(__form.getAfterDeliveryCost()) : null);
                    ps.setString(i++, joinChoices(__form.getTools(), __form.getToolsOther()));
                    ps.setString(i++, trim(__form.getToolCount()));
                    ps.setString(i++, SurveyFormModel.normalizeUsd(__form.getMoveHours()));
                    ps.setString(i++, joinChoices(__form.getFormats(), __form.getFormatsOther()));
                    ps.setString(i++, blindLabel(__form.getBlindOption(), __form.getBlindUsd()));
                    ps.setString(i++, trim(__form.getRevealBuy()));
                    ps.setString(i++, trim(__form.getRevealRenew()));
                    ps.setString(i++, trim(__form.getName()));
                    ps.setString(i++, trim(__form.getContact()));
                    ps.setString(i++, trim(__form.getTrial()));
                    // the browser's word only (D33): "1" or nothing, anything else counts as 0
                    ps.setInt(i++, "1".equals(__form.getBlindLocked()) ? 1 : 0);
                    ps.executeUpdate();
                }
            }
            return true;
        } catch (Exception ex) {
            log.error("Failed to save content survey to {}", dbPath, ex);
            return false;
        }
    }

    /** "其他" becomes "其他：<the respondent's words>", so one column holds the whole answer. */
    private String withOther(String __choice, String __other) {
        if (__choice == null || __choice.isBlank()) return null;
        String other = trim(__other);
        return SurveyContentFormModel.OTHER.equals(__choice) && other != null ? __choice + "：" + other : __choice;
    }

    /** Checked options in page order, joined with "、"; null when none is checked. */
    private String joinChoices(List<String> __checked, String __other) {
        List<String> labels = __checked.stream().map(c -> withOther(c, __other)).filter(c -> c != null).toList();
        return labels.isEmpty() ? null : String.join("、", labels);
    }

    /** A picked option (不會付費 / 不知道) wins over a typed amount, same as the verification survey. */
    private String blindLabel(String __option, String __usd) {
        if (__option != null && SurveyContentFormModel.BLIND_OPTIONS.contains(__option)) return __option;
        String usd = SurveyFormModel.normalizeUsd(__usd);
        return usd == null ? null : "US$" + usd;
    }

    // "other" is stored as its own words in the same column, so the existing table needs no new column
    private String situationLabel(String __situation, String __situationOther) {
        if (__situation == null) return null;
        switch (__situation) {
            case "verify":    return "確認 AI 產出的內容對不對";
            case "explain":   return "回應別人問「我們怎麼用 AI」";
            case "inventory": return "盤點舊系統、舊程式裡有什麼";
            case "other":     return "其他：" + trim(__situationOther);
            case "none":      return "以上都沒發生過";
            default:          return __situation;
        }
    }

    // "none" reads differently per question; "unknown" is always 不知道
    private String optionLabel(String __option, String __noneLabel) {
        if ("none".equals(__option)) return __noneLabel;
        if ("unknown".equals(__option)) return "不知道";
        return null;
    }

    private String extraSpendLabel(String __extraSpend, String __extraSpendUsd) {
        String label = optionLabel(__extraSpend, "沒有");
        if (label != null) return label;
        String usd = SurveyFormModel.normalizeUsd(__extraSpendUsd);
        return usd == null ? null : "US$" + usd;
    }

    private String trim(String __value) {
        return (__value == null || __value.isBlank()) ? null : __value.trim();
    }
}
